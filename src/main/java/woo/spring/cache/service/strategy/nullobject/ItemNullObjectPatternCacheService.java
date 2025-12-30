package woo.spring.cache.service.strategy.nullobject;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
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
public class ItemNullObjectPatternCacheService implements ItemCacheService {

    private final ItemService itemService;

    private static final ItemResponse nullObject = new ItemResponse(null, null);

    @Override
    public boolean supports(CacheStrategy cacheStrategy) {
        return CacheStrategy.NULL_OBJECT_PATTERN.equals(cacheStrategy);
    }

    @Override
    @Cacheable(cacheNames = "item", key = "#itemId")
    public ItemResponse read(Long itemId) {
        ItemResponse itemResponse = itemService.read(itemId);
        
        if (itemResponse == null) {
            return nullObject;
        }
        return itemResponse;
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
        return itemService.create(request);
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
