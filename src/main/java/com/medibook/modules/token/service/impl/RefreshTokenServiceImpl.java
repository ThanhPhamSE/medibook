package com.medibook.modules.token.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.medibook.common.exception.UnauthorizedException;
import com.medibook.modules.token.entity.RefreshToken;
import com.medibook.modules.token.mapper.RefreshTokenMapper;
import com.medibook.modules.token.repository.RefreshTokenRepository;
import com.medibook.modules.token.service.RefreshTokenService;
import com.medibook.modules.user.entity.User;
import com.medibook.security.jwt.JwtProperties;
import com.medibook.security.jwt.TokenHashUtil;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenMapper refreshTokenMapper;
    private final JwtProperties jwtProperties;
    private final TokenHashUtil tokenHashUtil;

    @Override
    public String createRefreshToken(User user, String deviceInfo, String ipAddress) {

        String rawToken = UUID.randomUUID().toString().replace("-", "");

        String tokenHash = tokenHashUtil.sha256(rawToken);

        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(jwtProperties.getRefreshTokenExpiration() / 1000);

        RefreshToken refreshToken = refreshTokenMapper.toEntity(user, tokenHash, expiresAt, deviceInfo, ipAddress);

        refreshTokenRepository.save(refreshToken);

        return rawToken;
    }

    @Override
    public RefreshToken verifyToken(String rawToken) {

        String tokenHash = tokenHashUtil.sha256(rawToken);

        RefreshToken token = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (Boolean.TRUE.equals(token.getRevoked())) {
            throw new UnauthorizedException("Refresh token revoked");
        }

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new UnauthorizedException("Refresh token expired");
        }

        return token;
    }

    @Override
    public void revokeToken(String rawToken) {

        String tokenHash = tokenHashUtil.sha256(rawToken);

        refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
        });
    }

    @Override
    public void revokeAllUserTokens(Long userId) {

        List<RefreshToken> tokens = refreshTokenRepository.findAllByUserId(userId);

        tokens.forEach(t -> t.setRevoked(true));

        refreshTokenRepository.saveAll(tokens);
    }

}
