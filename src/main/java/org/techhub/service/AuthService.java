package org.techhub.service;

import org.techhub.dto.request.LoginRequest;
import org.techhub.dto.request.RefreshTokenRequest;
import org.techhub.dto.request.RegisterRequest;
import org.techhub.dto.response.AuthResponse;

public interface AuthService {
    String register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refreshToken(RefreshTokenRequest request);
    void logout(String username);
}
