package woo.spring.cache.service.strategy.none;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import woo.spring.cache.common.cache.CacheStrategy;
import woo.spring.cache.common.cache.CustomCacheEvict;
import woo.spring.cache.common.cache.CustomCachePut;
import woo.spring.cache.common.cache.CustomCacheable;
import woo.spring.cache.model.ItemCreateReqeust;
import woo.spring.cache.model.ItemUpdateRequest;
import woo.spring.cache.service.ItemCacheService;
import woo.spring.cache.service.ItemService;
import woo.spring.cache.service.response.ItemPageResponse;
import woo.spring.cache.service.response.ItemResponse;

@Service
@RequiredArgsConstructor
public class ItemNoneCacheService implements ItemCacheService {

    // ItemCacheService(캐시전략 처리 후) -> ItemService(비지니스 로직 처리)

    private final ItemService itemService;

    @Override
    public boolean supports(CacheStrategy cacheStrategy) {
        return CacheStrategy.NONE.equals(cacheStrategy);
    }


    @Override
    @CustomCacheable(
            cacheStrategy = CacheStrategy.NONE,
            cacheName = "item",
            key = "#itemId",
            ttlSeconds = 5
    )
    public ItemResponse read(Long itemId) {
        return itemService.read(itemId);
    }

    @Override
    @CustomCacheable(
            cacheStrategy = CacheStrategy.NONE,
            cacheName = "itemList",
            key = "#page + ':' + #pageSize",
            ttlSeconds = 5
    )
    public ItemPageResponse readAll(Long page, Long pageSize) {
        return itemService.readAll(page, pageSize);
    }

    @Override
    @CustomCacheable(
            cacheStrategy = CacheStrategy.NONE,
            cacheName = "itemListInfiniteScroll",
            key = "#lastItemId + ':' + #pageSize",
            ttlSeconds = 5
    )
    public ItemPageResponse readAllInfiniteScroll(Long lastItemId, Long pageSize) {
        return itemService.readAllInfiniteScroll(lastItemId, pageSize);
    }

    @Override
    public ItemResponse create(ItemCreateReqeust request) {
        return itemService.create(request);
    }

    @Override
    @CustomCachePut(
            cacheStrategy = CacheStrategy.NONE,
            cacheName = "item",
            key = "#itemId",
            ttlSeconds = 5
    )
    public ItemResponse update(Long itemId, ItemUpdateRequest request) {
        return itemService.update(itemId, request);
    }

    @Override
    @CustomCacheEvict(
            cacheStrategy = CacheStrategy.NONE,
            cacheName = "item",
            key = "#itemId"
    )
    public void delete(Long itemId) {
        itemService.delete(itemId);
    }
}
