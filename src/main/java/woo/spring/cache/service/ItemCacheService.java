package woo.spring.cache.service;

import woo.spring.cache.common.cache.CacheStrategy;
import woo.spring.cache.model.ItemCreateReqeust;
import woo.spring.cache.model.ItemUpdateRequest;
import woo.spring.cache.service.response.ItemPageResponse;
import woo.spring.cache.service.response.ItemResponse;

public interface ItemCacheService {

    boolean supports(CacheStrategy cacheStrategy);

    ItemResponse read(Long itemId);

    ItemPageResponse readAll(Long page, Long pageSize);

    ItemPageResponse readAllInfiniteScroll(Long lastItemId, Long pageSize);

    ItemResponse create(ItemCreateReqeust request);

    ItemResponse update(Long itemId, ItemUpdateRequest request);

    void delete(Long itemId);
}
