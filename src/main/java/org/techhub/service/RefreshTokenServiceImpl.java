package org.techhub.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.techhub.entity.RefreshToken;
import org.techhub.entity.User;
import org.techhub.exception.ResourceNotFoundException;
import org.techhub.exception.TokenRefreshException;
import org.techhub.repository.RefreshTokenRepository;
import org.techhub.repository.UserRepository;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    @Value("${app.jwt.refresh-expiration-ms}")
    private Long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public RefreshToken createRefreshToken(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        // Invalidate or delete any existing refresh token for the user
        refreshTokenRepository.deleteByUser(user);
        refreshTokenRepository.flush();

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshTokenDurationMs))
                .revoked(false)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.isRevoked()) {
            throw new TokenRefreshException(token.getToken(), "Refresh token has been revoked. Please sign in again.");
        }

        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException(token.getToken(), "Refresh token has expired. Please sign in again.");
        }

        return token;
    }

    @Override
    @Transactional
    public RefreshToken rotateRefreshToken(String requestToken) {
        RefreshToken existingToken = refreshTokenRepository.findByToken(requestToken)
                .orElseThrow(() -> new TokenRefreshException(requestToken, "Refresh token not found in database."));

        // Verify if token is valid
        verifyExpiration(existingToken);

        User user = existingToken.getUser();

        // Refresh token rotation: Revoke / Delete old token and issue a brand new one
        refreshTokenRepository.delete(existingToken);
        refreshTokenRepository.flush();

        RefreshToken newRefreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshTokenDurationMs))
                .revoked(false)
                .build();

        return refreshTokenRepository.save(newRefreshToken);
    }

    @Override
    @Transactional
    public void revokeByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        refreshTokenRepository.deleteByUser(user);
    }
}
