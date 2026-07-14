package com.medibook.modules.auth.service;

import com.medibook.modules.auth.dto.request.ChangePasswordRequest;
import com.medibook.modules.auth.dto.request.ForgotPasswordRequest;
import com.medibook.modules.auth.dto.request.LoginRequest;
import com.medibook.modules.auth.dto.request.RegisterRequest;
import com.medibook.modules.auth.dto.request.ResetPasswordRequest;
import com.medibook.modules.auth.dto.response.ChangePasswordResponse;
import com.medibook.modules.auth.dto.response.ForgotPasswordResponse;
import com.medibook.modules.auth.dto.response.LoginResponse;
import com.medibook.modules.auth.dto.response.RegisterResponse;
import com.medibook.modules.auth.dto.response.ResetPasswordResponse;
import com.medibook.modules.user.dto.response.UserResponse;

public interface AuthService {

    RegisterResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    LoginResponse refreshToken(String refreshToken);

    void logout(String refreshToken);

    void logoutAllDevices(Long userId);

    ChangePasswordResponse changePassword(Long userId, ChangePasswordRequest request);

    ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request);

    ResetPasswordResponse resetPassword(ResetPasswordRequest request);

    void verifyEmail(String token);

    void resendVerificationEmail(String email);

    UserResponse getCurrentUser(Long userId);
}
