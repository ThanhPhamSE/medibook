package com.medibook.modules.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.medibook.common.response.ApiResponse;
import com.medibook.common.exception.UnauthorizedException;
import com.medibook.modules.user.dto.request.UpdateProfileRequest;
import com.medibook.modules.user.dto.response.UserResponse;
import com.medibook.modules.user.service.UserService;
import com.medibook.security.util.SecurityUtils;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {

        Long userId = SecurityUtils.getCurrentUserId();

        if (userId == null) {
            throw new UnauthorizedException("User not authenticated");
        }

        UserResponse user = userService.getUserById(userId);

        return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", user));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {

        Long userId = SecurityUtils.getCurrentUserId();

        if (userId == null) {
            throw new UnauthorizedException("User not authenticated");
        }

        UserResponse user = userService.updateProfile(userId, request);

        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", user));
    }
}
