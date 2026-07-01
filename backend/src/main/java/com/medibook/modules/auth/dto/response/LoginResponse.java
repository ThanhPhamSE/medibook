package com.medibook.modules.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {

    private Long userId;
    private String email;
    private String fullName;
    private String role;

    private String accessToken;
    private String refreshToken;

    private String tokenType;

    private long accessTokenExpiresAt;
    private long refreshTokenExpiresAt;

    private long issuedAt;
}
