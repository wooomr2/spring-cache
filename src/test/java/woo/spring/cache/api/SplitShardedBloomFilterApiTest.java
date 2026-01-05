package woo.spring.cache.api;

import org.junit.jupiter.api.Test;
import woo.spring.cache.common.cache.CacheStrategy;
import woo.spring.cache.model.ItemCreateReqeust;

public class SplitShardedBloomFilterApiTest {

    static final CacheStrategy CACHE_STRATEGY = CacheStrategy.SPLIT_BLOOM_FILTER;

    @Test
    void test() {
        for (int i = 0; i < 1000; i++) {
            ItemApiTestUtils.create(CACHE_STRATEGY, new ItemCreateReqeust("data" + i));
        }

        for (long itemId = 10000; itemId < 20000; itemId++) {
            ItemApiTestUtils.read(CACHE_STRATEGY, itemId);
        }
    }git
}
