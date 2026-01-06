package woo.spring.cache.service.strategy.sub;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import woo.spring.cache.service.strategy.sharded.SplitShardedBloomFilter;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SplitShardedSubBloomFilterTest {

    private static final Logger log = LoggerFactory.getLogger(SplitShardedSubBloomFilterTest.class);

    @Test
    void create() {
        SplitShardedSubBloomFilter splitShardedSubBloomFilter = SplitShardedSubBloomFilter.create(
                "testId", 1000, 0.01, 4
        );

        System.out.println("splitShardedSubBloomFilter: " + splitShardedSubBloomFilter);
        assertThat(splitShardedSubBloomFilter.getId()).isEqualTo("testId");
    }

    @Test
    void findSubFilter() {
        SplitShardedSubBloomFilter splitShardedSubBloomFilter = SplitShardedSubBloomFilter.create(
                "testId", 1000, 0.01, 4
        );

        SplitShardedBloomFilter subFilter0 = splitShardedSubBloomFilter.findSubFilter(0);
        SplitShardedBloomFilter subFilter1 = splitShardedSubBloomFilter.findSubFilter(1);
        SplitShardedBloomFilter subFilter2 = splitShardedSubBloomFilter.findSubFilter(2);

        log.info("subFilter0: " + subFilter0);
        log.info("subFilter1: " + subFilter1);
        log.info("subFilter2: " + subFilter2);

        assertThat(subFilter0.getId()).isEqualTo(splitShardedSubBloomFilter.getId() + ":sub:0");
        assertThat(subFilter0.getDataCount()).isEqualTo(splitShardedSubBloomFilter.getSplitShardedBloomFilter().getDataCount() * 2);
        assertThat(subFilter0.getFalsePositiveRate()).isEqualTo(splitShardedSubBloomFilter.getSplitShardedBloomFilter().getFalsePositiveRate() / 2);
        assertThat(subFilter0.getShardCount()).isEqualTo(splitShardedSubBloomFilter.getSplitShardedBloomFilter().getShardCount());

        assertThat(subFilter1.getId()).isEqualTo(splitShardedSubBloomFilter.getId() + ":sub:1");
        assertThat(subFilter1.getDataCount()).isEqualTo(splitShardedSubBloomFilter.getSplitShardedBloomFilter().getDataCount() * 4);
        assertThat(subFilter1.getFalsePositiveRate()).isEqualTo(splitShardedSubBloomFilter.getSplitShardedBloomFilter().getFalsePositiveRate() / 4);
        assertThat(subFilter1.getShardCount()).isEqualTo(splitShardedSubBloomFilter.getSplitShardedBloomFilter().getShardCount());

        assertThat(subFilter2.getId()).isEqualTo(splitShardedSubBloomFilter.getId() + ":sub:2");
        assertThat(subFilter2.getDataCount()).isEqualTo(splitShardedSubBloomFilter.getSplitShardedBloomFilter().getDataCount() * 8);
        assertThat(subFilter2.getFalsePositiveRate()).isEqualTo(splitShardedSubBloomFilter.getSplitShardedBloomFilter().getFalsePositiveRate() / 8);
        assertThat(subFilter2.getShardCount()).isEqualTo(splitShardedSubBloomFilter.getSplitShardedBloomFilter().getShardCount());
    }

    @Test
    void findActiveFilter_shoudReturnOriginFilter_whenSubFilterNotExists() {

        // given
        SplitShardedSubBloomFilter splitShardedSubBloomFilter = SplitShardedSubBloomFilter.create(
                "testId", 1000, 0.01, 4
        );

        // when
        SplitShardedBloomFilter activeFilter = splitShardedSubBloomFilter.findActiveFilter(0);

        // then
        assertThat(activeFilter.getId()).isEqualTo(splitShardedSubBloomFilter.getSplitShardedBloomFilter().getId());
    }

    @Test
    void findActiveFilter_shouldReturnSubFilter_whenSubFilterExists() {

        // given
        SplitShardedSubBloomFilter splitShardedSubBloomFilter = SplitShardedSubBloomFilter.create(
                "testId", 1000, 0.01, 4
        );

        int subFilterCount = 3;

        // when
        SplitShardedBloomFilter activeFilter = splitShardedSubBloomFilter.findActiveFilter(subFilterCount);

        // then
        assertThat(activeFilter.getId()).isEqualTo(splitShardedSubBloomFilter.getSplitShardedBloomFilter().getId() + ":sub:2");
    }

    @Test
    void findAll() {
        // given
        SplitShardedSubBloomFilter splitShardedSubBloomFilter = SplitShardedSubBloomFilter.create(
                "testId", 1000, 0.01, 4
        );

        int subFilterCount = 3;

        // when
        List<SplitShardedBloomFilter> splitShardedBloomFilters = splitShardedSubBloomFilter.findAll(subFilterCount);

        assertThat(splitShardedBloomFilters.size()).isEqualTo(subFilterCount + 1);
        for (int subFilterIndex = 0; subFilterIndex < subFilterCount; subFilterIndex++) {
            SplitShardedBloomFilter subFilter = splitShardedBloomFilters.get(subFilterIndex + 1);
            assertThat(subFilter.getId()).isEqualTo(splitShardedSubBloomFilter.findSubFilter(subFilterIndex).getId());
        }
    }
}