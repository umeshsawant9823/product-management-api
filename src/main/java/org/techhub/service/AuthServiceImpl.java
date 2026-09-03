package org.techhub.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.techhub.dto.request.LoginRequest;
import org.techhub.dto.request.RefreshTokenRequest;
import org.techhub.dto.request.RegisterRequest;
import org.techhub.dto.response.AuthResponse;
import org.techhub.entity.RefreshToken;
import org.techhub.entity.Role;
import org.techhub.entity.User;
import org.techhub.exception.BadRequestException;
import org.techhub.repository.UserRepository;
import org.techhub.security.JwtUtils;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;

    @Override
    @Transactional
    public String register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username is already taken: " + request.getUsername());
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered: " + request.getEmail());
        }

        Role role = request.getRole() != null ? request.getRole() : Role.ROLE_USER;

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .build();

        userRepository.save(user);
        return "User registered successfully with role: " + role.name();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadRequestException("User not found: " + request.getUsername()));

        String accessToken = jwtUtils.generateTokenFromUsername(user.getUsername(), user.getRole().name());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getUsername());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        // Refresh token rotation: verifies old token and generates new refresh token
        RefreshToken newRefreshToken = refreshTokenService.rotateRefreshToken(request.getRefreshToken());
        User user = newRefreshToken.getUser();

        String newAccessToken = jwtUtils.generateTokenFromUsername(user.getUsername(), user.getRole().name());

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken.getToken())
                .tokenType("Bearer")
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    @Override
    public void logout(String username) {
        refreshTokenService.revokeByUsername(username);
    }
}
