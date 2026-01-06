package woo.spring.cache.service.strategy.sub;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import woo.spring.cache.RedisTestContainerSupport;
import woo.spring.cache.service.strategy.sharded.SplitShardedBloomFilter;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SplitShardedSubBloomFilterRedisHandlerTest extends RedisTestContainerSupport {

    @Autowired
    SplitShardedSubBloomFilterRedisHandler handler;

    private long getDataCount(SplitShardedBloomFilter splitShardedBloomFilter) {
        String result = redisTemplate.opsForValue().get(
                "split-sharded-sub-bloom-filter:data-count:%s".formatted(splitShardedBloomFilter.getId())
        );

        return result == null ? 0 : Long.parseLong(result);
    }

    private int getSubFilterCount(SplitShardedSubBloomFilter splitShardedSubBloomFilter) {
        String result = redisTemplate.opsForValue().get(
                "split-sharded-sub-bloom-filter:sub-filter-count:%s".formatted(splitShardedSubBloomFilter.getId())
        );

        return result == null ? 0 : Integer.parseInt(result);
    }

    @Test
    void add() {
        SplitShardedSubBloomFilter splitShardedSubBloomFilter = SplitShardedSubBloomFilter.create(
                "testId", 1000, 0.01, 4
        );

        handler.add(splitShardedSubBloomFilter, "value");

        assertThat(getSubFilterCount(splitShardedSubBloomFilter)).isEqualTo(0);
        assertThat(getDataCount(splitShardedSubBloomFilter.findActiveFilter(0))).isEqualTo(1);
    }

    @Test
    void add_shouldAdDSubFilter_whenFilterIsFull() {
        SplitShardedSubBloomFilter splitShardedSubBloomFilter = SplitShardedSubBloomFilter.create(
                "testId", 1000, 0.01, 4
        );

        int count = 1000 - 1;
        for (int i = 0; i < count; i++) {
            handler.add(splitShardedSubBloomFilter, "value" + i);
        }
        assertThat(getSubFilterCount(splitShardedSubBloomFilter)).isEqualTo(0);
        assertThat(getDataCount(splitShardedSubBloomFilter.findActiveFilter(0))).isEqualTo(999);

        handler.add(splitShardedSubBloomFilter, "value" + 1000);
        assertThat(getSubFilterCount(splitShardedSubBloomFilter)).isEqualTo(1);
        assertThat(getDataCount(splitShardedSubBloomFilter.findActiveFilter(1))).isEqualTo(0);
    }

    @Test
    void add_shouldNotAddSubFilter_whenSubFilterCountReachedMaxLimit() {
        SplitShardedSubBloomFilter splitShardedSubBloomFilter = SplitShardedSubBloomFilter.create(
                "testId", 1000, 0.01, 4
        );

        // 3번째 subFilter 생성 전
        int count = 1000 + 2000 + 4000 - 1;
        for (int i = 0; i < count; i++) {
            handler.add(splitShardedSubBloomFilter, "value" + i);
        }
        assertThat(getSubFilterCount(splitShardedSubBloomFilter)).isEqualTo(2);
        assertThat(getDataCount(splitShardedSubBloomFilter.findActiveFilter(2))).isEqualTo(3999);

        handler.add(splitShardedSubBloomFilter, "newValue");
        assertThat(getSubFilterCount(splitShardedSubBloomFilter)).isEqualTo(2);
        assertThat(getDataCount(splitShardedSubBloomFilter.findActiveFilter(2))).isEqualTo(4000);
    }

    @Test
    void mightContain() {
        SplitShardedSubBloomFilter splitShardedSubBloomFilter = SplitShardedSubBloomFilter.create(
                "testId", 1000, 0.01, 4
        );

        List<String> values = IntStream.range(0, 1000 + 2000).mapToObj(v -> "value" + v).toList();
        for (String value : values) {
            handler.add(splitShardedSubBloomFilter, value);
        }

        for (String value : values) {
            boolean result = handler.mightContain(splitShardedSubBloomFilter, value);
            assertThat(result).isTrue();
        }

        for (int i = 0; i < 1000; i++) {
            String value = "notAddedValue" + i;
            boolean result = handler.mightContain(splitShardedSubBloomFilter, value);
            if (result) {
                // false positive
                System.out.println("value: " + value);
            }
        }
    }
}