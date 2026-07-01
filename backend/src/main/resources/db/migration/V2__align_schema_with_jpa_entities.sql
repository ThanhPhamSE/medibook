-- =========================================
-- SPECIALTIES
-- SoftDeleteEntity
-- cần: created_at, updated_at, deleted_at
-- =========================================

ALTER TABLE specialties
ADD COLUMN updated_at TIMESTAMP NULL
DEFAULT CURRENT_TIMESTAMP
ON
UPDATE CURRENT_TIMESTAMP;


-- =========================================
-- DOCTORS
-- VersionedEntity
-- cần: version, created_at, updated_at, deleted_at
-- =========================================

ALTER TABLE doctors
ADD COLUMN updated_at TIMESTAMP NULL
DEFAULT CURRENT_TIMESTAMP
ON
UPDATE CURRENT_TIMESTAMP;


-- =========================================
-- DOCTOR WORKING PATTERNS
-- SoftDeleteEntity
-- cần: created_at, updated_at, deleted_at
-- =========================================

ALTER TABLE doctor_working_patterns
ADD COLUMN updated_at TIMESTAMP NULL
DEFAULT CURRENT_TIMESTAMP
ON
UPDATE CURRENT_TIMESTAMP;


-- =========================================
-- DOCTOR TIME OFF
-- SoftDeleteEntity
-- cần: created_at, updated_at, deleted_at
-- =========================================

ALTER TABLE doctor_time_off
ADD COLUMN updated_at TIMESTAMP NULL
DEFAULT CURRENT_TIMESTAMP
ON
UPDATE CURRENT_TIMESTAMP;


-- =========================================
-- APPOINTMENTS
-- VersionedEntity
-- cần:
-- version
-- created_at
-- updated_at
-- deleted_at
-- =========================================

ALTER TABLE appointments
ADD COLUMN deleted_at TIMESTAMP NULL;


-- =========================================
-- MEDICAL RECORDS
-- SoftDeleteEntity
-- cần: created_at, updated_at, deleted_at
-- =========================================

ALTER TABLE medical_records
ADD COLUMN updated_at TIMESTAMP NULL
DEFAULT CURRENT_TIMESTAMP
ON
UPDATE CURRENT_TIMESTAMP;


-- =========================================
-- REFRESH TOKENS
-- AuditableEntity
-- cần: created_at, updated_at
-- =========================================

ALTER TABLE refresh_tokens
ADD COLUMN updated_at TIMESTAMP NULL
DEFAULT CURRENT_TIMESTAMP
ON
UPDATE CURRENT_TIMESTAMP;


-- =========================================
-- REVIEWS
-- SoftDeleteEntity
-- cần: created_at, updated_at, deleted_at
-- =========================================

ALTER TABLE reviews
ADD COLUMN updated_at TIMESTAMP NULL
DEFAULT CURRENT_TIMESTAMP
ON
UPDATE CURRENT_TIMESTAMP;


-- =========================================
-- APPOINTMENT HISTORY
-- thêm index
-- =========================================

CREATE INDEX idx_history_changed_at
ON appointment_status_histories(changed_at);


-- =========================================
-- AUDIT LOG
-- thêm index
-- =========================================

CREATE INDEX idx_audit_created_at
ON audit_logs(created_at);