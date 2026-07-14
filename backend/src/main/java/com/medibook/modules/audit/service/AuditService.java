package com.medibook.modules.audit.service;

import org.springframework.data.domain.Pageable;
import com.medibook.common.response.PageResponse;
import com.medibook.modules.audit.dto.response.AuditLogResponse;

public interface AuditService {

    void log(String action, String entityType, Long entityId, Object oldValue, Object newValue);

    PageResponse<AuditLogResponse> getAllAuditLogs(Pageable pageable);
}

