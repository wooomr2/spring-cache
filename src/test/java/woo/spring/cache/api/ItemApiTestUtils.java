package woo.spring.cache.api;

import org.springframework.web.client.RestClient;
import woo.spring.cache.common.cache.CacheStrategy;
import woo.spring.cache.model.ItemCreateReqeust;
import woo.spring.cache.model.ItemUpdateRequest;
import woo.spring.cache.service.response.ItemPageResponse;
import woo.spring.cache.service.response.ItemResponse;

public class ItemApiTestUtils {

    static RestClient restClient = RestClient.create("http://localhost:8080");

    static ItemResponse read(CacheStrategy cacheStrategy, Long itemId) {
        return restClient.get()
                .uri("/cache-strategy/%s/items/%s".formatted(cacheStrategy.name(), itemId))
                .retrieve()
                .body(ItemResponse.class);
    }

    static ItemPageResponse readAll(CacheStrategy cacheStrategy, Long page, Long pageSize) {
        return restClient.get()
                .uri("/cache-strategy/%s/items?page=%s&pageSize=%s".formatted(cacheStrategy.name(), page, pageSize))
                .retrieve()
                .body(ItemPageResponse.class);
    }

    static ItemPageResponse readAllInfiniteScroll(CacheStrategy cacheStrategy, Long lastItemId, Long pageSize) {
        String uri = lastItemId == null ?
                "/cache-strategy/%s/items/infinite-scroll?pageSize=%s".formatted(cacheStrategy.name(), pageSize) :
                "/cache-strategy/%s/items/infinite-scroll?lastItemId=%s&pageSize=%s".formatted(cacheStrategy.name(), lastItemId, pageSize);

        return restClient.get()
                .uri(uri)
                .retrieve()
                .body(ItemPageResponse.class);
    }

    static ItemResponse create(CacheStrategy cacheStrategy, ItemCreateReqeust request) {
        return restClient.post()
                .uri("/cache-strategy/%s/items".formatted(cacheStrategy.name()))
                .body(request)
                .retrieve()
                .body(ItemResponse.class);
    }

    static ItemResponse update(CacheStrategy cacheStrategy, Long itemId, ItemUpdateRequest request) {
        return restClient.put()
                .uri("/cache-strategy/%s/items/%s".formatted(cacheStrategy.name(), itemId))
                .body(request)
                .retrieve()
                .body(ItemResponse.class);
    }

    static void delete(CacheStrategy cacheStrategy, Long itemId) {
        restClient.delete()
                .uri("/cache-strategy/%s/items/%s".formatted(cacheStrategy.name(), itemId))
                .retrieve()
                .toBodilessEntity();
    }
}
