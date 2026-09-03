package org.techhub.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.techhub.dto.request.ProductRequest;
import org.techhub.dto.response.PageResponse;
import org.techhub.dto.response.ProductResponse;
import org.techhub.service.ProductService;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("GET /api/v1/products should return 200 OK with paginated products")
    void getAllProducts_ShouldReturnOk() throws Exception {
        PageResponse<ProductResponse> pageResponse = PageResponse.<ProductResponse>builder()
                .content(Collections.emptyList())
                .pageNumber(0)
                .pageSize(10)
                .totalElements(0)
                .totalPages(0)
                .last(true)
                .build();

        when(productService.getAllProducts(anyInt(), anyInt(), anyString(), anyString(), any()))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.pageNumber").value(0));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("GET /api/v1/products/{id} should return 200 OK")
    void getProductById_ShouldReturnOk() throws Exception {
        ProductResponse response = ProductResponse.builder()
                .id(1L)
                .productName("Sample Product")
                .createdBy("admin")
                .createdOn(LocalDateTime.now())
                .build();

        when(productService.getProductById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.productName").value("Sample Product"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("POST /api/v1/products as ADMIN should create product and return 201 Created")
    void createProduct_AsAdmin_ShouldReturnCreated() throws Exception {
        ProductRequest request = ProductRequest.builder()
                .productName("New Keyboard")
                .build();

        ProductResponse response = ProductResponse.builder()
                .id(1L)
                .productName("New Keyboard")
                .createdBy("admin")
                .createdOn(LocalDateTime.now())
                .build();

        when(productService.createProduct(any(ProductRequest.class), anyString())).thenReturn(response);

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.productName").value("New Keyboard"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("POST /api/v1/products with invalid body should return 400 Bad Request")
    void createProduct_InvalidInput_ShouldReturnBadRequest() throws Exception {
        ProductRequest invalidRequest = ProductRequest.builder()
                .productName("") // blank
                .build();

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("POST /api/v1/products as regular USER should return 403 Forbidden")
    void createProduct_AsRegularUser_ShouldReturnForbidden() throws Exception {
        ProductRequest request = ProductRequest.builder()
                .productName("Unauthorized Product")
                .build();

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
