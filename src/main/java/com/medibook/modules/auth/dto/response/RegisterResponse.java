package com.medibook.modules.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RegisterResponse {

    private Long userId;

    private String email;

    private String fullName;

    private String role;

}
