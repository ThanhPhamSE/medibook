package com.medibook.modules.auth.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.medibook.common.exception.UnauthorizedException;
import com.medibook.common.response.ApiResponse;
import com.medibook.modules.auth.dto.request.ChangePasswordRequest;
import com.medibook.modules.auth.dto.request.ForgotPasswordRequest;
import com.medibook.modules.auth.dto.request.LoginRequest;
import com.medibook.modules.auth.dto.request.LogoutRequest;
import com.medibook.modules.auth.dto.request.RefreshTokenRequest;
import com.medibook.modules.auth.dto.request.RegisterRequest;
import com.medibook.modules.auth.dto.request.ResetPasswordRequest;
import com.medibook.modules.auth.dto.response.ForgotPasswordResponse;
import com.medibook.modules.auth.dto.response.LoginResponse;
import com.medibook.modules.auth.dto.response.RegisterResponse;
import com.medibook.modules.auth.dto.response.ResetPasswordResponse;
import com.medibook.modules.auth.service.AuthService;
import com.medibook.modules.user.dto.response.UserResponse;
import com.medibook.security.util.SecurityUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication and authorization APIs")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Create a new user account with email verification")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(@Valid @RequestBody RegisterRequest request) {

        request.normalize();

        RegisterResponse response = authService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "Register successful", response));

    }

    @PostMapping("/resend-verification")
    @Operation(summary = "Resend verification email", description = "Resend email verification to user")
    public ResponseEntity<ApiResponse<Void>> resendVerification(
            @RequestBody Map<String, String> body) {
        authService.resendVerificationEmail(body.get("email"));
        return ResponseEntity.ok(ApiResponse.success("Email sent", null));
    }

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and return JWT tokens")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {

        LoginResponse response = authService.login(request);

        return ResponseEntity.ok(ApiResponse.success("Login successful", response));

    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh access token", description = "Refresh JWT access token using refresh token")
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(@RequestBody RefreshTokenRequest request) {

        LoginResponse response = authService.refreshToken(request.getRefreshToken());

        return ResponseEntity.ok(ApiResponse.success("Refresh token successful", response));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Logout user from current device")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody LogoutRequest request) {

        authService.logout(request.getRefreshToken());

        return ResponseEntity.ok(ApiResponse.success("Logout successful", null));
    }

    @PostMapping("/logout-all")
    @Operation(summary = "Logout all devices", description = "Logout user from all devices")
    public ResponseEntity<ApiResponse<Void>> logoutAll() {

        Long userId = SecurityUtils.getCurrentUserId();

        if (userId == null) {
            throw new UnauthorizedException("User not authenticated");
        }

        authService.logoutAllDevices(userId);

        return ResponseEntity.ok(ApiResponse.success("Logout all devices successful", null));
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change password", description = "Change user password (requires authentication)")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {

        Long userId = SecurityUtils.getCurrentUserId();

        authService.changePassword(userId, request);

        // Logout all devices after password change for security
        authService.logoutAllDevices(userId);

        return ResponseEntity.ok(ApiResponse.success("Password changed successfully. Please login again.", null));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Forgot password", description = "Request password reset email")
    public ResponseEntity<ApiResponse<ForgotPasswordResponse>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        ForgotPasswordResponse response = authService.forgotPassword(request);

        return ResponseEntity.ok(ApiResponse.success("Request processed", response));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password", description = "Reset password using token from email")
    public ResponseEntity<ApiResponse<ResetPasswordResponse>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        ResetPasswordResponse response = authService.resetPassword(request);

        return ResponseEntity.ok(ApiResponse.success("Password reset successfully", response));
    }

    @PostMapping("/verify-email")
    @Operation(summary = "Verify email", description = "Verify user email using token")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(
            @Parameter(description = "Email verification token") @RequestParam String token) {

        authService.verifyEmail(token);

        return ResponseEntity.ok(ApiResponse.success("Account verified successfully", null));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Retrieve current authenticated user information")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {

        Long userId = SecurityUtils.getCurrentUserId();

        if (userId == null) {
            throw new UnauthorizedException("User not authenticated");
        }

        UserResponse user = authService.getCurrentUser(userId);

        return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", user));
    }
}
