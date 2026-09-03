package org.techhub.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.techhub.dto.request.LoginRequest;
import org.techhub.dto.request.RefreshTokenRequest;
import org.techhub.dto.request.RegisterRequest;
import org.techhub.dto.response.ApiResponse;
import org.techhub.dto.response.AuthResponse;
import org.techhub.service.AuthService;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user registration, login, and token rotation")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Registers a new user with USER or ADMIN role")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegisterRequest request) {
        String message = authService.register(request);
        return new ResponseEntity<>(ApiResponse.success(message, null), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticates user and returns JWT access token & refresh token")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse authResponse = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Rotate refresh token", description = "Validates existing refresh token, rotates it, and returns new access token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse authResponse = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", authResponse));
    }

    @PostMapping("/logout")
    @Operation(summary = "User logout", description = "Invalidates the active refresh token for the logged in user")
    public ResponseEntity<ApiResponse<String>> logout(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            authService.logout(userDetails.getUsername());
        }
        return ResponseEntity.ok(ApiResponse.success("Logout successful", null));
    }
}
