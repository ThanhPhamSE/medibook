package com.medibook.modules.audit.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.medibook.common.response.ApiResponse;
import com.medibook.common.response.PageResponse;
import com.medibook.modules.audit.dto.response.AuditLogResponse;
import com.medibook.modules.audit.service.AuditService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/audit-logs")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasRole('ADMIN')")
public class AdminAuditLogController {

    private final AuditService auditService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AuditLogResponse>>> getAuditLogs(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(auditService.getAllAuditLogs(pageable)));
    }
}
