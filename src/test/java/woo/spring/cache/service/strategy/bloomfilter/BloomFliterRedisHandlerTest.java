package woo.spring.cache.service.strategy.bloomfilter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import woo.spring.cache.RedisTestContainerSupport;

import java.time.Duration;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
class BloomFliterRedisHandlerTest extends RedisTestContainerSupport {

    @Autowired
    BloomFliterRedisHandler bloomFilterRedisHandler;
    @Autowired
    private BloomFliterRedisHandler bloomFliterRedisHandler;

    @Test
    void add() {
        // given
        BloomFilter bloomFilter = BloomFilter.create("testId", 1000, 0.01);

        // when
        bloomFilterRedisHandler.add(bloomFilter, "value");

        // then
        List<Long> hashedIndexes = bloomFilter.hash("value");

        for (long offset = 0; offset < bloomFilter.getBitSize(); offset++) {
            Boolean result = redisTemplate.opsForValue().getBit("bloom-filter:" + bloomFilter.getId(), offset);
            assertThat(result).isEqualTo(hashedIndexes.contains(offset));
        }
    }

    @Test
    void delete() {
        // given
        BloomFilter bloomFilter = BloomFilter.create("testId", 1000, 0.01);
        bloomFilterRedisHandler.add(bloomFilter, "value");

        // when
        bloomFliterRedisHandler.delete(bloomFilter);

        // then
        for (long offset = 0; offset < bloomFilter.getBitSize(); offset++) {
            Boolean result = redisTemplate.opsForValue().getBit("bloom-filter:" + bloomFilter.getId(), offset);
            assertThat(result).isFalse();
        }
    }

    @Test
    void mightContain() {
        // given
        BloomFilter bloomFilter = BloomFilter.create("testId", 1000, 0.01);

        List<String> values = IntStream.range(0, 1000).mapToObj(i -> "value" + i).toList();
        for (String value : values) {
            bloomFilterRedisHandler.add(bloomFilter, value);
        }

        // when
        for (String value : values) {
            boolean result = bloomFilterRedisHandler.mightContain(bloomFilter, value);
            assertThat(result).isTrue();
        }

        for (int i = 0; i < 10000; i++) {
            String value = "notAddedValue" + i;
            boolean result = bloomFilterRedisHandler.mightContain(bloomFilter, value);
            if (result) {
                // false positive
                System.out.println("value: " + value);
            }
        }
    }

    @Test
    void printExecutionTime_addToLatgeBloomFilter() {
        BloomFilter bloomFilter = BloomFilter.create("testId", 400_000_000, 0.01);
        List<Long> hashedIndexes = bloomFilter.hash("value");
        System.out.println("bitSize: " + bloomFilter.getBitSize());
        System.out.println("hasedIndexes: " + hashedIndexes);

        long start = System.nanoTime();
        bloomFilterRedisHandler.add(bloomFilter, "vluae");
        long millis = Duration.ofNanos(System.nanoTime() - start).toMillis();
        System.out.println("millis: " + millis);
    }

    @Test
    void printExecutionTime_addToLatgeBloomFilterAfterInit() {
        BloomFilter bloomFilter = BloomFilter.create("testId", 400_000_000, 0.01);
        List<Long> hashedIndexes = bloomFilter.hash("value");
        System.out.println("bitSize: " + bloomFilter.getBitSize());
        System.out.println("hasedIndexes: " + hashedIndexes);

        bloomFilterRedisHandler.init(bloomFilter);

        long start = System.nanoTime();
        bloomFilterRedisHandler.add(bloomFilter, "vluae");
        long millis = Duration.ofNanos(System.nanoTime() - start).toMillis();
        System.out.println("millis: " + millis);
    }
}