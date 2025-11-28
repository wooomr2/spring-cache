package woo.spring.cache.service.response;

import woo.spring.cache.model.Item;

public record ItemResponse(Long itemId, String data) {

    public static ItemResponse from(Item item) {
        return new ItemResponse(item.getItemId(), item.getData());
    }
}
