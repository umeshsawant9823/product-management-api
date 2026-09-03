package org.techhub.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.techhub.dto.request.ProductRequest;
import org.techhub.dto.response.ApiResponse;
import org.techhub.dto.response.PageResponse;
import org.techhub.dto.response.ProductResponse;
import org.techhub.service.ProductService;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Product Management", description = "Endpoints for managing products with CRUD, pagination, and sorting")
@SecurityRequirement(name = "Bearer Authentication")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "Get all products", description = "Returns a paginated list of products with sorting and search capabilities")
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String search
    ) {
        PageResponse<ProductResponse> response = productService.getAllProducts(page, size, sortBy, sortDir, search);
        return ResponseEntity.ok(ApiResponse.success("Products fetched successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID", description = "Returns product details by its unique identifier")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable Long id) {
        ProductResponse response = productService.getProductById(id);
        return ResponseEntity.ok(ApiResponse.success("Product fetched successfully", response));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create product", description = "Creates a new product (ADMIN role required)")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody ProductRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String username = userDetails != null ? userDetails.getUsername() : "SYSTEM";
        ProductResponse response = productService.createProduct(request, username);
        return new ResponseEntity<>(ApiResponse.success("Product created successfully", response), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update product", description = "Updates an existing product by ID (ADMIN role required)")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String username = userDetails != null ? userDetails.getUsername() : "SYSTEM";
        ProductResponse response = productService.updateProduct(id, request, username);
        return ResponseEntity.ok(ApiResponse.success("Product updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete product", description = "Deletes a product by ID (ADMIN role required)")
    public ResponseEntity<ApiResponse<String>> deleteProduct(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String username = userDetails != null ? userDetails.getUsername() : "SYSTEM";
        productService.deleteProduct(id, username);
        return ResponseEntity.ok(ApiResponse.success("Product deleted successfully", null));
    }
}
