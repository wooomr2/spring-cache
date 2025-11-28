package woo.spring.cache.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import woo.spring.cache.model.Item;
import woo.spring.cache.model.ItemCreateReqeust;
import woo.spring.cache.model.ItemUpdateRequest;
import woo.spring.cache.repository.ItemRepository;
import woo.spring.cache.service.response.ItemPageResponse;
import woo.spring.cache.service.response.ItemResponse;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    public ItemResponse read(Long itemId) {
        return itemRepository.read(itemId).map(ItemResponse::from).orElse(null);
    }

    public ItemPageResponse readAll(Long page, Long pageSize) {
        return ItemPageResponse.from(
                itemRepository.readAll(page, pageSize),
                itemRepository.count()
        );
    }

    public ItemPageResponse readAllInfiniteScroll(Long lastItemId, Long pageSize) {
        return ItemPageResponse.from(
                itemRepository.readAllInfiniteScroll(lastItemId, pageSize),
                itemRepository.count()
        );
    }

    public ItemResponse create(ItemCreateReqeust request) {
        return ItemResponse.from(itemRepository.create(Item.create(request)));
    }

    public ItemResponse update(Long itemId, ItemUpdateRequest request) {
        Item item = itemRepository.read(itemId).orElseThrow();
        item.update(request);
        return ItemResponse.from(
                itemRepository.update(item)
        );
    }

    public void delete(Long itemId) {
        itemRepository.read(itemId).ifPresent(itemRepository::delete);
    }

    public long count() {
        return itemRepository.count();
    }
}
