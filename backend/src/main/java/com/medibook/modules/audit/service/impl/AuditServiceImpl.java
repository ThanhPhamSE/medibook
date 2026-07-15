package com.medibook.modules.audit.service.impl;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medibook.common.response.PageResponse;
import com.medibook.common.response.util.PageMapper;
import com.medibook.common.util.RequestUtils;
import com.medibook.modules.audit.dto.response.AuditLogResponse;
import com.medibook.modules.audit.entity.AuditLog;
import com.medibook.modules.audit.repository.AuditLogRepository;
import com.medibook.modules.audit.service.AuditService;
import com.medibook.modules.user.repository.UserRepository;
import com.medibook.security.util.SecurityUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final RequestUtils requestUtils;

    @Override
    @Async("auditExecutor")
    public void log(String action, String entityType, Long entityId, Object oldValue, Object newValue) {

        AuditLog auditLog = new AuditLog();

        auditLog.setAction(action);
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);

        Long userId = SecurityUtils.getCurrentUserId();

        if (userId != null) {
            auditLog.setUser(userRepository.findById(userId).orElse(null));
        }

        try {
            auditLog.setIpAddress(requestUtils.getClientIp());
            auditLog.setUserAgent(requestUtils.getUserAgent());
        } catch (Exception e) {
            // HTTP request context may not be available in async threads
        }

        try {
            auditLog.setOldValue(oldValue == null ? null : objectMapper.writeValueAsString(oldValue));

            auditLog.setNewValue(newValue == null ? null : objectMapper.writeValueAsString(newValue));

        } catch (Exception e) {
            auditLog.setOldValue("SERIALIZATION_ERROR");
            auditLog.setNewValue("SERIALIZATION_ERROR");
        }

        auditLog.setCreatedAt(LocalDateTime.now());

        auditLogRepository.save(auditLog);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> getAllAuditLogs(Pageable pageable) {
        Page<AuditLog> page = auditLogRepository.findAll(pageable);
        Page<AuditLogResponse> responsePage = page.map(log -> {
            String actor = log.getUser() != null ? log.getUser().getFullName() : "system";
            String target = log.getEntityType() + (log.getEntityId() != null ? ":" + log.getEntityId() : "");
            return AuditLogResponse.builder()
                    .id(log.getId())
                    .actor(actor)
                    .action(log.getAction())
                    .target(target)
                    .ip(log.getIpAddress() != null ? log.getIpAddress() : "127.0.0.1")
                    .timestamp(log.getCreatedAt())
                    .build();
        });
        return PageMapper.from(responsePage);
    }

}
