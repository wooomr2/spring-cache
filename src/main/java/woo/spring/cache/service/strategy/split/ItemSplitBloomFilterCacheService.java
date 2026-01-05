package woo.spring.cache.service.strategy.split;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import woo.spring.cache.common.cache.CacheStrategy;
import woo.spring.cache.model.ItemCreateReqeust;
import woo.spring.cache.model.ItemUpdateRequest;
import woo.spring.cache.service.ItemCacheService;
import woo.spring.cache.service.ItemService;
import woo.spring.cache.service.response.ItemPageResponse;
import woo.spring.cache.service.response.ItemResponse;

@Service
@RequiredArgsConstructor
public class ItemSplitBloomFilterCacheService implements ItemCacheService {

    private final ItemService itemService;
    private final SplitBloomFilterRedisHandler splitBloomFilterRedisHandler;

    private static final SplitBloomFilter bloomFliter = SplitBloomFilter.create(
            "item-bloom-filter",
            1000,
            0.01
    );

    @Override
    public boolean supports(CacheStrategy cacheStrategy) {
        return CacheStrategy.SPLIT_BLOOM_FILTER.equals(cacheStrategy);
    }

    @Override
    public ItemResponse read(Long itemId) {
        boolean result = splitBloomFilterRedisHandler.mightContain(bloomFliter, String.valueOf(itemId));
        if (!result) {
            return null;
        }
        return itemService.read(itemId);
    }

    @Override
    public ItemPageResponse readAll(Long page, Long pageSize) {
        return itemService.readAll(page, pageSize);
    }

    @Override
    public ItemPageResponse readAllInfiniteScroll(Long lastItemId, Long pageSize) {
        return itemService.readAllInfiniteScroll(lastItemId, pageSize);
    }

    @Override
    public ItemResponse create(ItemCreateReqeust request) {
        ItemResponse itemResponse = itemService.create(request);
        splitBloomFilterRedisHandler.add(bloomFliter, String.valueOf(itemResponse.itemId()));
        return itemResponse;
    }

    @Override
    public ItemResponse update(Long itemId, ItemUpdateRequest request) {
        return itemService.update(itemId, request);
    }

    @Override
    public void delete(Long itemId) {
        itemService.delete(itemId);
    }
}
