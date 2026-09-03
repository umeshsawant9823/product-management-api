package org.techhub.service;

import org.techhub.dto.request.ProductRequest;
import org.techhub.dto.response.PageResponse;
import org.techhub.dto.response.ProductResponse;

public interface ProductService {
    PageResponse<ProductResponse> getAllProducts(int page, int size, String sortBy, String sortDir, String search);
    ProductResponse getProductById(Long id);
    ProductResponse createProduct(ProductRequest request, String username);
    ProductResponse updateProduct(Long id, ProductRequest request, String username);
    void deleteProduct(Long id, String username);
}
