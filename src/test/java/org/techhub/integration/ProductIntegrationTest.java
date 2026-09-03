package org.techhub.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.techhub.dto.request.ItemRequest;
import org.techhub.dto.request.LoginRequest;
import org.techhub.dto.request.ProductRequest;
import org.techhub.dto.request.RegisterRequest;
import org.techhub.entity.Role;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        // Register and login an admin user for the integration tests
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("integrationadmin")
                .email("admin@integration.com")
                .password("adminpass123")
                .role(Role.ROLE_ADMIN)
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)));

        LoginRequest loginRequest = LoginRequest.builder()
                .username("integrationadmin")
                .password("adminpass123")
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

        adminToken = objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data")
                .path("accessToken")
                .asText();
    }

    @Test
    @DisplayName("End-to-End Product & Item Lifecycle with JWT Auth")
    void testFullProductLifecycle() throws Exception {
        // 1. Create Product
        ProductRequest productRequest = ProductRequest.builder()
                .productName("Integration Test Monitor 4K")
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.productName").value("Integration Test Monitor 4K"))
                .andReturn();

        long productId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .path("data")
                .path("id")
                .asLong();

        // 2. Add Item to Product
        ItemRequest itemRequest = ItemRequest.builder()
                .quantity(25)
                .build();

        mockMvc.perform(post("/api/v1/products/" + productId + "/items")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.quantity").value(25));

        // 3. Get Items for Product
        mockMvc.perform(get("/api/v1/products/" + productId + "/items")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].quantity").value(25));

        // 4. Update Product
        ProductRequest updateRequest = ProductRequest.builder()
                .productName("Integration Test Monitor 4K Ultra")
                .build();

        mockMvc.perform(put("/api/v1/products/" + productId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.productName").value("Integration Test Monitor 4K Ultra"));

        // 5. Query Products with Pagination and Search
        mockMvc.perform(get("/api/v1/products?search=Monitor&page=0&size=5")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());

        // 6. Delete Product
        mockMvc.perform(delete("/api/v1/products/" + productId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 7. Verify Deleted Product returns 404
        mockMvc.perform(get("/api/v1/products/" + productId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }
}
