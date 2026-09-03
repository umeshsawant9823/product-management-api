package org.techhub.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.techhub.dto.request.LoginRequest;
import org.techhub.dto.request.RefreshTokenRequest;
import org.techhub.dto.request.RegisterRequest;
import org.techhub.entity.Role;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Complete Auth Flow: Register -> Login -> Refresh Token Rotation")
    void testAuthFlowAndRefreshTokenRotation() throws Exception {
        // 1. Register a new user
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("testauthuser")
                .email("testauth@example.com")
                .password("password123")
                .role(Role.ROLE_ADMIN)
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));

        // 2. Login with credentials
        LoginRequest loginRequest = LoginRequest.builder()
                .username("testauthuser")
                .password("password123")
                .build();

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isString())
                .andExpect(jsonPath("$.data.refreshToken").isString())
                .andReturn();

        String responseString = loginResult.getResponse().getContentAsString();
        String initialRefreshToken = objectMapper.readTree(responseString)
                .path("data")
                .path("refreshToken")
                .asText();

        // 3. Perform Refresh Token Rotation
        RefreshTokenRequest refreshRequest = RefreshTokenRequest.builder()
                .refreshToken(initialRefreshToken)
                .build();

        MvcResult refreshResult = mockMvc.perform(post("/api/v1/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isString())
                .andExpect(jsonPath("$.data.refreshToken").isString())
                .andReturn();

        String newRefreshToken = objectMapper.readTree(refreshResult.getResponse().getContentAsString())
                .path("data")
                .path("refreshToken")
                .asText();

        // 4. Verify that the old refresh token is no longer valid (Token Rotation Enforcement)
        mockMvc.perform(post("/api/v1/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isForbidden());
    }
}
