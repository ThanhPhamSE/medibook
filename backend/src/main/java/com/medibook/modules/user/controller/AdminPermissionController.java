package com.medibook.modules.user.controller;

import java.util.List;
import java.util.Arrays;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.medibook.common.response.ApiResponse;
import com.medibook.modules.user.dto.response.PermissionInfoResponse;

@RestController
@RequestMapping("/api/v1/admin/permissions")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPermissionController {

    @GetMapping
    public ResponseEntity<ApiResponse<List<PermissionInfoResponse>>> getPermissions() {
        List<PermissionInfoResponse> permissions = Arrays.asList(
            new PermissionInfoResponse("p-1", "Appointments", "view", Arrays.asList("ADMIN", "DOCTOR", "CUSTOMER")),
            new PermissionInfoResponse("p-2", "Appointments", "create", Arrays.asList("ADMIN", "DOCTOR", "CUSTOMER")),
            new PermissionInfoResponse("p-3", "Appointments", "cancel", Arrays.asList("ADMIN", "DOCTOR", "CUSTOMER")),
            new PermissionInfoResponse("p-4", "Medical Records", "view", Arrays.asList("ADMIN", "DOCTOR")),
            new PermissionInfoResponse("p-5", "Medical Records", "create", Arrays.asList("ADMIN", "DOCTOR")),
            new PermissionInfoResponse("p-6", "Users", "manage", Arrays.asList("ADMIN")),
            new PermissionInfoResponse("p-7", "Reports", "view", Arrays.asList("ADMIN")),
            new PermissionInfoResponse("p-8", "Settings", "manage", Arrays.asList("ADMIN")),
            new PermissionInfoResponse("p-9", "Audit Logs", "view", Arrays.asList("ADMIN"))
        );
        return ResponseEntity.ok(ApiResponse.success(permissions));
    }
}
