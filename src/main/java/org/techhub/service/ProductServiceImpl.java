package org.techhub.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.techhub.dto.request.ProductRequest;
import org.techhub.dto.response.ItemResponse;
import org.techhub.dto.response.PageResponse;
import org.techhub.dto.response.ProductResponse;
import org.techhub.entity.Product;
import org.techhub.exception.ResourceNotFoundException;
import org.techhub.repository.ProductRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final AsyncAuditService asyncAuditService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> getAllProducts(int page, int size, String sortBy, String sortDir, String search) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> productPage;

        if (search != null && !search.trim().isEmpty()) {
            productPage = productRepository.findByProductNameContainingIgnoreCase(search.trim(), pageable);
        } else {
            productPage = productRepository.findAll(pageable);
        }

        List<ProductResponse> content = productPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.<ProductResponse>builder()
                .content(content)
                .pageNumber(productPage.getNumber())
                .pageSize(productPage.getSize())
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .last(productPage.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        return mapToResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest request, String username) {
        Product product = Product.builder()
                .productName(request.getProductName())
                .createdBy(username != null ? username : "SYSTEM")
                .createdOn(LocalDateTime.now())
                .build();

        Product savedProduct = productRepository.save(product);

        // Async audit logging
        asyncAuditService.logProductActivity("CREATE", savedProduct.getId(), username,
                "Product created with name: " + savedProduct.getProductName());

        return mapToResponse(savedProduct);
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request, String username) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        product.setProductName(request.getProductName());
        product.setModifiedBy(username != null ? username : "SYSTEM");
        product.setModifiedOn(LocalDateTime.now());

        Product updatedProduct = productRepository.save(product);

        // Async audit logging
        asyncAuditService.logProductActivity("UPDATE", updatedProduct.getId(), username,
                "Product updated with name: " + updatedProduct.getProductName());

        return mapToResponse(updatedProduct);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id, String username) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        productRepository.delete(product);

        // Async audit logging
        asyncAuditService.logProductActivity("DELETE", id, username,
                "Product deleted: " + product.getProductName());
    }

    private ProductResponse mapToResponse(Product product) {
        List<ItemResponse> itemResponses = product.getItems() != null
                ? product.getItems().stream()
                .map(item -> ItemResponse.builder()
                        .id(item.getId())
                        .productId(product.getId())
                        .quantity(item.getQuantity())
                        .build())
                .collect(Collectors.toList())
                : Collections.emptyList();

        return ProductResponse.builder()
                .id(product.getId())
                .productName(product.getProductName())
                .createdBy(product.getCreatedBy())
                .createdOn(product.getCreatedOn())
                .modifiedBy(product.getModifiedBy())
                .modifiedOn(product.getModifiedOn())
                .items(itemResponses)
                .build();
    }
}
