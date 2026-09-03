package org.techhub.service;

import org.techhub.entity.RefreshToken;

public interface RefreshTokenService {
    RefreshToken createRefreshToken(String username);
    RefreshToken verifyExpiration(RefreshToken token);
    RefreshToken rotateRefreshToken(String requestToken);
    void revokeByUsername(String username);
}
