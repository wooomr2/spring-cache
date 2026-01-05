package woo.spring.cache.service.strategy.split;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import woo.spring.cache.RedisTestContainerSupport;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class SplitBloomFilterRedisHandlerTest extends RedisTestContainerSupport {

    @Autowired
    SplitBloomFilterRedisHandler splitBloomFilterRedisHandler;

    @Test
    void mightContain() {
        SplitBloomFilter splitBloomFilter = SplitBloomFilter.create("tsetId", 1000, 0.01);

        List<String> values = IntStream.range(0, 1000).mapToObj(idx -> "value" + idx).toList();
        for (String value : values) {
            splitBloomFilterRedisHandler.add(splitBloomFilter, value);
        }

        // when, then
        for (String value : values) {
            boolean result = splitBloomFilterRedisHandler.mightContain(splitBloomFilter, value);
            assertTrue(result);
        }

        for (int i = 0; i < 10000; i++) {
            String value = "notAddedValue" + i;
            boolean result = splitBloomFilterRedisHandler.mightContain(splitBloomFilter, value);
            if (result) {
                // false positive
                System.out.println("value + " + value);
            }
        }

    }

}