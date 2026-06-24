package com.medibook.modules.audit.service;

public interface AuditService {
    void log(String action, String entityType, Long entityId, Object oldValue, Object newValue);
}
