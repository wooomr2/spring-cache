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
    BloomFilterRedisHandler bloomFilterRedisHandler;
    @Autowired
    private BloomFilterRedisHandler bloomFliterRedisHandler;

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

    /**
     * [한계]
     * BloomFilter는 입력되는 데이터 수가 많아질수록 오차율이 증가한다.(입력된 데이터를 삭제할 수 도 없다)
     * 초기에 n개의 데이터 입력을 가정했다면, n개를 초과하는 시점부터 오차율이 점점 증가하게 된다.
     * 이미 만들어진 BloomFilter는 도중에 크기를 늘릴수도 없다(이미 고정된 크기범위에 대해 해시 함수가 동작하기 떄문)
     * <p>
     * -> 처음부터 BloomFilter를 크게 만든다(but Redis 메모리제한, 초기에 불필요하게 큰 메모리를 할당해야함)
     * -> 그럼 다른 방안은? (Split / Sharding / Sub-Filter)
     */
    @Test
    void mightContain_whenBloomFilterAddedToMany() {
        // given
        BloomFilter bloomFilter = BloomFilter.create("testId", 1000, 0.01);

        List<String> values = IntStream.range(0, 2000).mapToObj(i -> "value" + i).toList();
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