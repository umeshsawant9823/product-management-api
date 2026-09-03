package org.techhub.service;

import org.techhub.dto.request.ItemRequest;
import org.techhub.dto.response.ItemResponse;

import java.util.List;

public interface ItemService {
    List<ItemResponse> getItemsByProductId(Long productId);
    ItemResponse addItemToProduct(Long productId, ItemRequest request);
    void deleteItem(Long productId, Long itemId);
}
