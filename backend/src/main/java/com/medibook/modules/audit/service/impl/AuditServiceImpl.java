package com.medibook.modules.audit.service.impl;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medibook.common.util.RequestUtils;
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
    @Async
    public void log(String action, String entityType, Long entityId, Object oldValue, Object newValue) {

        AuditLog auditLog = new AuditLog();

        auditLog.setAction(action);
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);

        Long userId = SecurityUtils.getCurrentUserId();

        if (userId != null) {
            auditLog.setUser(userRepository.findById(userId).orElse(null));
        }

        auditLog.setIpAddress(requestUtils.getClientIp());
        auditLog.setUserAgent(requestUtils.getUserAgent());

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

}
