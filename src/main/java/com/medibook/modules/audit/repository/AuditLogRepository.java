package com.medibook.modules.audit.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.medibook.modules.audit.entity.AuditLog;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

}
