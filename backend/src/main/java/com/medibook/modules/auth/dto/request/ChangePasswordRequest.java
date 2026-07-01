package com.medibook.modules.auth.dto.request;

import com.medibook.common.validation.annotation.StrongPassword;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordRequest {

    @NotBlank(message = "Current password is required")
    private String currentPassword;

    @NotBlank(message = "New password is required")
    @StrongPassword
    private String newPassword;

    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;
}
