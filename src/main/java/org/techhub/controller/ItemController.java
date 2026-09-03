package org.techhub.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.techhub.dto.request.ItemRequest;
import org.techhub.dto.response.ApiResponse;
import org.techhub.dto.response.ItemResponse;
import org.techhub.service.ItemService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products/{id}/items")
@RequiredArgsConstructor
@Tag(name = "Product Items", description = "Endpoints for managing items associated with a product")
@SecurityRequirement(name = "Bearer Authentication")
public class ItemController {

    private final ItemService itemService;

    @GetMapping
    @Operation(summary = "Get items of a product", description = "Retrieves all items associated with the specified product ID")
    public ResponseEntity<ApiResponse<List<ItemResponse>>> getItemsByProductId(@PathVariable("id") Long productId) {
        List<ItemResponse> items = itemService.getItemsByProductId(productId);
        return ResponseEntity.ok(ApiResponse.success("Items fetched successfully", items));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add item to product", description = "Adds a new item with quantity to the specified product (ADMIN role required)")
    public ResponseEntity<ApiResponse<ItemResponse>> addItemToProduct(
            @PathVariable("id") Long productId,
            @Valid @RequestBody ItemRequest request
    ) {
        ItemResponse item = itemService.addItemToProduct(productId, request);
        return new ResponseEntity<>(ApiResponse.success("Item added to product successfully", item), HttpStatus.CREATED);
    }

    @DeleteMapping("/{itemId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete item from product", description = "Deletes an item from the specified product (ADMIN role required)")
    public ResponseEntity<ApiResponse<String>> deleteItem(
            @PathVariable("id") Long productId,
            @PathVariable("itemId") Long itemId
    ) {
        itemService.deleteItem(productId, itemId);
        return ResponseEntity.ok(ApiResponse.success("Item deleted successfully", null));
    }
}
