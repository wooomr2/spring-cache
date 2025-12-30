package woo.spring.cache.api;

import org.junit.jupiter.api.Test;
import woo.spring.cache.common.cache.CacheStrategy;
import woo.spring.cache.model.ItemCreateReqeust;
import woo.spring.cache.model.ItemUpdateRequest;
import woo.spring.cache.service.response.ItemPageResponse;
import woo.spring.cache.service.response.ItemResponse;

public class NullObjectPatternCacheStrategyApiTest {
    static final CacheStrategy CACHE_STRATEGY = CacheStrategy.NULL_OBJECT_PATTERN;

    @Test
    void createAndReadAndUpdateAndDelete() {
        ItemResponse item1 = ItemApiTestUtils.create(CACHE_STRATEGY, new ItemCreateReqeust("data1"));
        ItemResponse item2 = ItemApiTestUtils.create(CACHE_STRATEGY, new ItemCreateReqeust("data2"));
        ItemResponse item3 = ItemApiTestUtils.create(CACHE_STRATEGY, new ItemCreateReqeust("data3"));

        ItemResponse item1read1 = ItemApiTestUtils.read(CACHE_STRATEGY, item1.itemId());
        ItemResponse item1read2 = ItemApiTestUtils.read(CACHE_STRATEGY, item1.itemId());
        ItemResponse item1read3 = ItemApiTestUtils.read(CACHE_STRATEGY, item1.itemId());
        System.out.println("read1: " + item1read1);
        System.out.println("read2: " + item1read2);
        System.out.println("read3: " + item1read3);

        ItemResponse item2read1 = ItemApiTestUtils.read(CACHE_STRATEGY, item2.itemId());
        ItemResponse item2read2 = ItemApiTestUtils.read(CACHE_STRATEGY, item2.itemId());
        ItemResponse item2read3 = ItemApiTestUtils.read(CACHE_STRATEGY, item2.itemId());
        System.out.println("read1: " + item2read1);
        System.out.println("read2: " + item2read2);
        System.out.println("read3: " + item2read3);

        ItemResponse item3read1 = ItemApiTestUtils.read(CACHE_STRATEGY, item3.itemId());
        ItemResponse item3read2 = ItemApiTestUtils.read(CACHE_STRATEGY, item3.itemId());
        ItemResponse item3read3 = ItemApiTestUtils.read(CACHE_STRATEGY, item3.itemId());
        System.out.println("read1: " + item3read1);
        System.out.println("read2: " + item3read2);
        System.out.println("read3: " + item3read3);

        // readAll
        ItemPageResponse itemReadAll1 = ItemApiTestUtils.readAll(CACHE_STRATEGY, 1L, 2L);
        ItemPageResponse itemReadAll2 = ItemApiTestUtils.readAll(CACHE_STRATEGY, 1L, 2L);
        System.out.println("itemReadAll1: " + itemReadAll1);
        System.out.println("itemReadAll2: " + itemReadAll2);

        // readInfiniteScroll
        ItemPageResponse itemReadAllInfinite1 = ItemApiTestUtils.readAllInfiniteScroll(CACHE_STRATEGY, null, 2L);
        ItemPageResponse itemReadAllInfinite2 = ItemApiTestUtils.readAllInfiniteScroll(CACHE_STRATEGY, itemReadAllInfinite1.items().getLast().itemId(), 2L);
        System.out.println("itemReadAllInfinite1: " + itemReadAllInfinite1);
        System.out.println("itemReadAllInfinite2: " + itemReadAllInfinite2);

        // update
        ItemApiTestUtils.update(CACHE_STRATEGY, item1.itemId(), new ItemUpdateRequest("updatedData"));
        ItemResponse updated = ItemApiTestUtils.read(CACHE_STRATEGY, item1.itemId());
        System.out.println("updated: " + updated);

        // delete
        ItemApiTestUtils.delete(CACHE_STRATEGY, item1.itemId());
        try {
            ItemApiTestUtils.read(CACHE_STRATEGY, item1.itemId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void readNullData() {
        for (int i = 0; i < 3; i++) {
            try {
                ItemResponse itemResponse = ItemApiTestUtils.read(CACHE_STRATEGY, 99999L);
                System.out.println(itemResponse);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
