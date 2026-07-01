package com.medibook.modules.token.service;

import com.medibook.modules.token.entity.RefreshToken;
import com.medibook.modules.user.entity.User;

public interface RefreshTokenService {

    String createRefreshToken(User user, String deviceInfo, String ipAddress);

    RefreshToken verifyToken(String rawToken);

    void revokeToken(String rawToken);

    void revokeAllUserTokens(Long userId);
}
