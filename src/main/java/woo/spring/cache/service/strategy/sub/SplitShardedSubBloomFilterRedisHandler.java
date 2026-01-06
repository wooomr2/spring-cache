package woo.spring.cache.service.strategy.sub;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import woo.spring.cache.common.lock.DistributedLockProvider;
import woo.spring.cache.service.strategy.sharded.SplitShardedBloomFilter;
import woo.spring.cache.service.strategy.sharded.SplitShardedBloomFilterRedisHandler;

import java.time.Duration;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SplitShardedSubBloomFilterRedisHandler {

    private final StringRedisTemplate redisTemplate;
    private final DistributedLockProvider distributedLockProvider;
    private final SplitShardedBloomFilterRedisHandler splitShardedBloomFilterRedisHandler;

    public static final int MAX_SUB_FILTER_COUNT = 2;

    private String genDataCountKey(SplitShardedBloomFilter splitShardedBloomFilter) {
        return "split-sharded-sub-bloom-filter:data-count:%s".formatted(splitShardedBloomFilter.getId());
    }

    private String genSubFilterCountKey(SplitShardedSubBloomFilter splitShardedSubBloomFilter) {
        return "split-sharded-sub-bloom-filter:sub-filter-count:%s".formatted(splitShardedSubBloomFilter.getId());
    }

    private String genSubFilterCountDistributedLockKey(SplitShardedSubBloomFilter splitShardedSubBloomFilter) {
        return "split-sharded-sub-bloom-filter:sub-filter-count:%s:distributed-lock".formatted(splitShardedSubBloomFilter.getId());
    }

    public void init(SplitShardedSubBloomFilter splitShardedSubBloomFilter) {
        SplitShardedBloomFilter splitShardedBloomFilter = splitShardedSubBloomFilter.getSplitShardedBloomFilter();
        splitShardedBloomFilterRedisHandler.init(splitShardedBloomFilter);
    }

    public void add(SplitShardedSubBloomFilter splitShardedSubBloomFilter, String value) {
        int subFilterCount = findSubFilterCount(splitShardedSubBloomFilter);
        SplitShardedBloomFilter activated = splitShardedSubBloomFilter.findActiveFilter(subFilterCount);
        splitShardedBloomFilterRedisHandler.add(activated, value);

        Long dataCount = redisTemplate.opsForValue().increment(genDataCountKey(activated));
        appendSubFilterIfFull(splitShardedSubBloomFilter, activated, dataCount);
    }

    private int findSubFilterCount(SplitShardedSubBloomFilter splitShardedSubBloomFilter) {
        String result = redisTemplate.opsForValue().get(genSubFilterCountKey(splitShardedSubBloomFilter));
        if (!StringUtils.hasText(result)) {
            return 0;
        }
        return Integer.parseInt(result);
    }

    private void appendSubFilterIfFull(SplitShardedSubBloomFilter splitShardedSubBloomFilter, SplitShardedBloomFilter activated, Long dataCount) {

        if (!isFull(activated, dataCount)) {
            return;
        }

        String distributedLockKey = genSubFilterCountDistributedLockKey(splitShardedSubBloomFilter);
        if (!distributedLockProvider.lock(distributedLockKey, Duration.ofMinutes(1))) {
            return;
        }

        try {
            int subFilterCount = findSubFilterCount(splitShardedSubBloomFilter);
            if (subFilterCount >= MAX_SUB_FILTER_COUNT) {
                log.warn("sub-filter limit reached. id = {}, usbFilterCount = {}",
                        splitShardedSubBloomFilter.getId(), subFilterCount
                );
                return;
            }

            splitShardedBloomFilterRedisHandler.init(splitShardedSubBloomFilter.findSubFilter(subFilterCount));
            redisTemplate.opsForValue().increment(genSubFilterCountKey(splitShardedSubBloomFilter));

        } finally {
            distributedLockProvider.unlock(distributedLockKey);
        }
    }

    public boolean mightContain(SplitShardedSubBloomFilter splitShardedSubBloomFilter, String value) {
        int subFilterCount = findSubFilterCount(splitShardedSubBloomFilter);

        return splitShardedSubBloomFilter.findAll(subFilterCount)
                .stream()
                .anyMatch(splitShardedBloomFilter ->
                        splitShardedBloomFilterRedisHandler.mightContain(splitShardedBloomFilter, value)
                );
    }

    public void delete(SplitShardedSubBloomFilter splitShardedSubBloomFilter) {
        int subFilterCount = findSubFilterCount(splitShardedSubBloomFilter);
        List<SplitShardedBloomFilter> filters = splitShardedSubBloomFilter.findAll(subFilterCount);
        for (SplitShardedBloomFilter filter : filters) {
            splitShardedBloomFilterRedisHandler.delete(filter);
            redisTemplate.delete(genDataCountKey(filter));
        }

    }

    private boolean isFull(SplitShardedBloomFilter activated, Long dataCount) {
        return dataCount != null && activated.getDataCount() <= dataCount;
    }
}
