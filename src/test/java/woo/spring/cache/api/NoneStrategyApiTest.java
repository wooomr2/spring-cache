package woo.spring.cache.api;

import org.junit.jupiter.api.Test;
import woo.spring.cache.common.cache.CacheStrategy;
import woo.spring.cache.model.ItemCreateReqeust;
import woo.spring.cache.model.ItemUpdateRequest;
import woo.spring.cache.service.response.ItemPageResponse;
import woo.spring.cache.service.response.ItemResponse;

public class NoneStrategyApiTest {
    static final CacheStrategy CACHE_STRATEGY = CacheStrategy.NONE;

    @Test
    void createAndReadAndUpdateAndDelete() {
        ItemResponse created = ItemApiTestUtils.create(CACHE_STRATEGY, new ItemCreateReqeust("data"));
        System.out.println("created: " + created);

        ItemResponse read1 = ItemApiTestUtils.read(CACHE_STRATEGY, created.itemId());
        System.out.println("read1: " + read1);

        ItemResponse updated = ItemApiTestUtils.update(CACHE_STRATEGY, read1.itemId(), new ItemUpdateRequest("updatedData"));
        System.out.println("updated: " + updated);

        ItemResponse read2 = ItemApiTestUtils.read(CACHE_STRATEGY, created.itemId());
        System.out.println("read2: " + read2);

        ItemApiTestUtils.delete(CACHE_STRATEGY, created.itemId());
        ItemResponse read3 = ItemApiTestUtils.read(CACHE_STRATEGY, created.itemId());
        System.out.println("read3: " + read3);
    }

    @Test
    void readAll() {
        for (int i = 0; i < 3; i++) {
            ItemApiTestUtils.create(CACHE_STRATEGY, new ItemCreateReqeust("data" + i));
        }

        ItemPageResponse itemPage1 = ItemApiTestUtils.readAll(CACHE_STRATEGY, 1L, 2L);
        System.out.println("itemPage1: " + itemPage1);

        ItemPageResponse itemPage2 = ItemApiTestUtils.readAll(CACHE_STRATEGY, 2L, 2L);
        System.out.println("itemPage2: " + itemPage2);
    }

    @Test
    void readAllInfiniteScroll() {
        for (int i = 0; i < 3; i++) {
            ItemApiTestUtils.create(CACHE_STRATEGY, new ItemCreateReqeust("data" + i));
        }

        ItemPageResponse itemPage1 = ItemApiTestUtils.readAllInfiniteScroll(CACHE_STRATEGY, null, 2L);
        System.out.println("itemPage1: " + itemPage1);

        long itemId = itemPage1.items().getLast().itemId();
        System.out.println("itemId: " + itemId);

        ItemPageResponse itemPage2 = ItemApiTestUtils.readAllInfiniteScroll(
                CACHE_STRATEGY,
                itemId,
                2L
        );
        System.out.println("itemPage2: " + itemPage2);
    }
}
