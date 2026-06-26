package com.medibook.modules.user.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.medibook.common.response.PageResponse;
import com.medibook.modules.user.dto.request.UserSearchRequest;
import com.medibook.modules.user.dto.response.UserResponse;
import com.medibook.modules.user.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    public PageResponse<UserResponse> getAll(UserSearchRequest request, Pageable pageable) {
        return userService.getAllUsers(request, pageable);
    }

    @GetMapping("/{id}")
    public UserResponse getById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @PatchMapping("/{id}/activate")
    public void activate(@PathVariable Long id) {
        userService.activateUser(id);
    }

    @PatchMapping("/{id}/deactivate")
    public void deactivate(@PathVariable Long id) {
        userService.deactivateUser(id);
    }
}
