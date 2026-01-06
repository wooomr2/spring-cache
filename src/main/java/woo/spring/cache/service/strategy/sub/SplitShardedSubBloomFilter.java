package woo.spring.cache.service.strategy.sub;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import woo.spring.cache.service.strategy.sharded.SplitShardedBloomFilter;

import java.util.ArrayList;
import java.util.List;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SplitShardedSubBloomFilter {

    private String id;
    private SplitShardedBloomFilter splitShardedBloomFilter;

    public static SplitShardedSubBloomFilter create(String id, long dataCount, double falsePositiveRate, int shardCount) {
        SplitShardedSubBloomFilter splitShardedSubBloomFilter = new SplitShardedSubBloomFilter();
        splitShardedSubBloomFilter.id = id;
        splitShardedSubBloomFilter.splitShardedBloomFilter = SplitShardedBloomFilter.create(
                id, dataCount, falsePositiveRate, shardCount
        );
        return splitShardedSubBloomFilter;
    }

    public SplitShardedBloomFilter findSubFilter(int subFilterIndex) {
        return SplitShardedBloomFilter.create(
                id + ":sub:" + subFilterIndex,
                splitShardedBloomFilter.getDataCount() * (1L << (subFilterIndex + 1)),
                splitShardedBloomFilter.getFalsePositiveRate() / (1L << (subFilterIndex + 1)),
                splitShardedBloomFilter.getShardCount()
        );
    }

    public SplitShardedBloomFilter findActiveFilter(int subFilterCount) {
        if (subFilterCount == 0) {
            return splitShardedBloomFilter;
        }
        int activeFilterindex = subFilterCount - 1;
        return findSubFilter(activeFilterindex);
    }

    public List<SplitShardedBloomFilter> findAll(int subFilterCount) {
        List<SplitShardedBloomFilter> splitShardedBloomFilters = new ArrayList<>();
        splitShardedBloomFilters.add(splitShardedBloomFilter);
        for (int subFilterIndex = 0; subFilterIndex < subFilterCount; subFilterIndex++) {
            splitShardedBloomFilters.add(findSubFilter(subFilterIndex));
        }
        return splitShardedBloomFilters;
    }
}
