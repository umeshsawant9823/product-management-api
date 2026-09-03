package org.techhub.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.techhub.dto.request.ItemRequest;
import org.techhub.dto.response.ItemResponse;
import org.techhub.entity.Item;
import org.techhub.entity.Product;
import org.techhub.exception.ResourceNotFoundException;
import org.techhub.repository.ItemRepository;
import org.techhub.repository.ProductRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ItemResponse> getItemsByProductId(Long productId) {
        // Ensure product exists
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product", "id", productId);
        }

        List<Item> items = itemRepository.findByProductId(productId);
        return items.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ItemResponse addItemToProduct(Long productId, ItemRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        Item item = Item.builder()
                .product(product)
                .quantity(request.getQuantity())
                .build();

        Item savedItem = itemRepository.save(item);
        return mapToResponse(savedItem);
    }

    @Override
    @Transactional
    public void deleteItem(Long productId, Long itemId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product", "id", productId);
        }

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item", "id", itemId));

        if (!item.getProduct().getId().equals(productId)) {
            throw new ResourceNotFoundException(
                    String.format("Item id %d does not belong to Product id %d", itemId, productId)
            );
        }

        itemRepository.delete(item);
    }

    private ItemResponse mapToResponse(Item item) {
        return ItemResponse.builder()
                .id(item.getId())
                .productId(item.getProduct() != null ? item.getProduct().getId() : null)
                .quantity(item.getQuantity())
                .build();
    }
}
