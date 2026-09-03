package org.techhub.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.techhub.dto.request.ProductRequest;
import org.techhub.dto.response.PageResponse;
import org.techhub.dto.response.ProductResponse;
import org.techhub.entity.Product;
import org.techhub.exception.ResourceNotFoundException;
import org.techhub.repository.ProductRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private AsyncAuditService asyncAuditService;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;
    private ProductRequest productRequest;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .id(1L)
                .productName("Test Laptop")
                .createdBy("admin")
                .createdOn(LocalDateTime.now())
                .items(new ArrayList<>())
                .build();

        productRequest = ProductRequest.builder()
                .productName("Test Laptop")
                .build();
    }

    @Test
    @DisplayName("Should create product successfully")
    void createProduct_Success() {
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductResponse response = productService.createProduct(productRequest, "admin");

        assertNotNull(response);
        assertEquals("Test Laptop", response.getProductName());
        assertEquals("admin", response.getCreatedBy());
        verify(productRepository, times(1)).save(any(Product.class));
        verify(asyncAuditService, times(1)).logProductActivity(eq("CREATE"), eq(1L), eq("admin"), anyString());
    }

    @Test
    @DisplayName("Should return product by ID when product exists")
    void getProductById_Success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductResponse response = productService.getProductById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Test Laptop", response.getProductName());
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when product does not exist")
    void getProductById_NotFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.getProductById(99L));
        verify(productRepository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("Should return paginated products")
    void getAllProducts_WithPagination() {
        List<Product> products = List.of(product);
        Page<Product> productPage = new PageImpl<>(products);

        when(productRepository.findAll(any(Pageable.class))).thenReturn(productPage);

        PageResponse<ProductResponse> response = productService.getAllProducts(0, 10, "id", "asc", null);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals(0, response.getPageNumber());
        assertEquals("Test Laptop", response.getContent().get(0).getProductName());
        verify(productRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("Should update product successfully")
    void updateProduct_Success() {
        ProductRequest updateRequest = ProductRequest.builder()
                .productName("Updated Laptop")
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductResponse response = productService.updateProduct(1L, updateRequest, "admin");

        assertNotNull(response);
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(any(Product.class));
        verify(asyncAuditService, times(1)).logProductActivity(eq("UPDATE"), eq(1L), eq("admin"), anyString());
    }

    @Test
    @DisplayName("Should delete product successfully")
    void deleteProduct_Success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        doNothing().when(productRepository).delete(product);

        assertDoesNotThrow(() -> productService.deleteProduct(1L, "admin"));

        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).delete(product);
        verify(asyncAuditService, times(1)).logProductActivity(eq("DELETE"), eq(1L), eq("admin"), anyString());
    }
}
