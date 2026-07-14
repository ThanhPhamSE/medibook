package com.medibook.modules.token.service;

import com.medibook.common.exception.UnauthorizedException;
import com.medibook.modules.token.entity.RefreshToken;
import com.medibook.modules.token.repository.RefreshTokenRepository;
import com.medibook.modules.token.service.impl.RefreshTokenServiceImpl;
import com.medibook.modules.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private com.medibook.security.jwt.TokenHashUtil tokenHashUtil;

    @InjectMocks
    private RefreshTokenServiceImpl refreshTokenService;

    private User user;
    private RefreshToken refreshToken;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        refreshToken = new RefreshToken();
        refreshToken.setId(1L);
        refreshToken.setUser(user);
        refreshToken.setTokenHash("valid-token");
        refreshToken.setDeviceInfo("WEB");
        refreshToken.setIpAddress("127.0.0.1");
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(7));
        refreshToken.setRevoked(false);
    }

    @Test
    void createRefreshToken_Success() {
        when(tokenHashUtil.sha256(anyString())).thenReturn("hashed-token");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(refreshToken);

        String token = refreshTokenService.createRefreshToken(user, "WEB", "127.0.0.1");

        assertThat(token).isNotNull();
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void verifyToken_Success() {
        when(tokenHashUtil.sha256("valid-token")).thenReturn("valid-token");
        when(refreshTokenRepository.findByTokenHash("valid-token")).thenReturn(Optional.of(refreshToken));

        RefreshToken verified = refreshTokenService.verifyToken("valid-token");

        assertThat(verified).isNotNull();
        assertThat(verified.getTokenHash()).isEqualTo("valid-token");
        verify(refreshTokenRepository).findByTokenHash("valid-token");
    }

    @Test
    void verifyToken_NotFound() {
        when(tokenHashUtil.sha256("invalid-token")).thenReturn("invalid-token");
        when(refreshTokenRepository.findByTokenHash("invalid-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenService.verifyToken("invalid-token"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Invalid refresh token");

        verify(refreshTokenRepository).findByTokenHash("invalid-token");
    }

    @Test
    void verifyToken_Expired() {
        refreshToken.setExpiresAt(LocalDateTime.now().minusDays(1));
        when(tokenHashUtil.sha256("expired-token")).thenReturn("expired-token");
        when(refreshTokenRepository.findByTokenHash("expired-token")).thenReturn(Optional.of(refreshToken));

        assertThatThrownBy(() -> refreshTokenService.verifyToken("expired-token"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Refresh token expired");

        verify(refreshTokenRepository).findByTokenHash("expired-token");
    }

    @Test
    void verifyToken_Revoked() {
        refreshToken.setRevoked(true);
        when(tokenHashUtil.sha256("revoked-token")).thenReturn("revoked-token");
        when(refreshTokenRepository.findByTokenHash("revoked-token")).thenReturn(Optional.of(refreshToken));

        assertThatThrownBy(() -> refreshTokenService.verifyToken("revoked-token"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Refresh token revoked");

        verify(refreshTokenRepository).findByTokenHash("revoked-token");
    }

    @Test
    void revokeToken_Success() {
        when(tokenHashUtil.sha256("valid-token")).thenReturn("valid-token");
        when(refreshTokenRepository.findByTokenHash("valid-token")).thenReturn(Optional.of(refreshToken));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(refreshToken);

        refreshTokenService.revokeToken("valid-token");

        assertThat(refreshToken.getRevoked()).isTrue();
        verify(refreshTokenRepository).save(refreshToken);
    }

    @Test
    void revokeToken_NotFound() {
        when(tokenHashUtil.sha256("invalid-token")).thenReturn("invalid-token");
        when(refreshTokenRepository.findByTokenHash("invalid-token")).thenReturn(Optional.empty());

        refreshTokenService.revokeToken("invalid-token");

        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    @Test
    void revokeAllUserTokens_Success() {
        when(refreshTokenRepository.findAllByUserId(1L)).thenReturn(java.util.List.of(refreshToken));
        when(refreshTokenRepository.saveAll(anyList())).thenReturn(java.util.List.of(refreshToken));

        refreshTokenService.revokeAllUserTokens(1L);

        assertThat(refreshToken.getRevoked()).isTrue();
        verify(refreshTokenRepository).findAllByUserId(1L);
        verify(refreshTokenRepository).saveAll(anyList());
    }

    @Test
    void revokeAllUserTokens_NoTokens() {
        when(refreshTokenRepository.findAllByUserId(1L)).thenReturn(java.util.List.of());

        refreshTokenService.revokeAllUserTokens(1L);

        verify(refreshTokenRepository).findAllByUserId(1L);
        verify(refreshTokenRepository, never()).saveAll(anyList());
    }
}
