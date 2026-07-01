-- ROLES
CREATE TABLE roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) UNIQUE NOT NULL
);
INSERT INTO roles (name) VALUES ('ADMIN'), ('DOCTOR'), ('CUSTOMER');

-- USERS
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_id BIGINT NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    gender ENUM('MALE', 'FEMALE', 'OTHER'),
    birth_date DATE,
    profile_image VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    
    -- INDEX: get user theo role (ADMIN / DOCTOR / CUSTOMER)
    INDEX idx_users_role (role_id),
    -- UNIQUE: email không trùng trong trạng thái active (soft delete)
    UNIQUE KEY uq_users_email_active (email, deleted_at),
    -- UNIQUE: số điện thoại không trùng (soft delete)
    UNIQUE KEY uq_users_phone_active (phone, deleted_at),
    
    FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE RESTRICT
);

-- SPECIALTIES: Chuyên nghành
CREATE TABLE specialties (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) UNIQUE NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL
);

-- DOCTORS
CREATE TABLE doctors (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE,
    specialty_id BIGINT NOT NULL,
    degree VARCHAR(255),
    experience_years INT DEFAULT 0,
    consultation_fee DECIMAL(10,2) NOT NULL,
    biography TEXT,
    average_rating DECIMAL(3,2) DEFAULT 0,
    total_reviews INT DEFAULT 0,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,

    CHECK (experience_years >= 0),
    CHECK (consultation_fee >= 0),
    CHECK (average_rating BETWEEN 0 AND 5),
    CHECK (total_reviews >= 0),
    
    -- INDEX: tìm bác sĩ theo chuyên khoa
    INDEX idx_doctor_specialty (specialty_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (specialty_id) REFERENCES specialties(id) ON DELETE RESTRICT
);

-- DOCTOR WORKING PATTERNS: lịch làm việc lặp theo tuần
CREATE TABLE doctor_working_patterns (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    doctor_id BIGINT NOT NULL,
    day_of_week ENUM('MON','TUE','WED','THU','FRI','SAT','SUN') NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    slot_duration INT NOT NULL,
    buffer_duration INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    
    CHECK (start_time < end_time),
    CHECK (slot_duration > 0),
    CHECK (buffer_duration >= 0),
    
    -- UNIQUE: tránh duplicate lịch lam viec trong 1 ngay
    -- UNIQUE KEY uq_work_pattern (doctor_id,day_of_week),
    FOREIGN KEY (doctor_id) REFERENCES doctors(id) ON DELETE CASCADE
);

-- DOCTOR TIME OFF: lịch nghỉ / không khả dụng
CREATE TABLE doctor_time_off (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    doctor_id BIGINT NOT NULL,
    start_datetime TIMESTAMP NOT NULL,
    end_datetime TIMESTAMP NOT NULL,
    reason VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    CHECK (start_datetime < end_datetime),
    -- INDEX: check bác sĩ có đang nghỉ trong khoảng thời gian không
    INDEX idx_timeoff_doctor (doctor_id, start_datetime, end_datetime),
    FOREIGN KEY (doctor_id) REFERENCES doctors(id) ON DELETE CASCADE
);

-- APPOINTMENTS: lịch đặt khám
CREATE TABLE appointments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    booking_code VARCHAR(50) UNIQUE NOT NULL,
    doctor_id BIGINT NOT NULL,
    patient_id BIGINT NOT NULL,
    consultation_fee DECIMAL(10,2) NOT NULL,
    start_datetime TIMESTAMP NOT NULL,
    end_datetime TIMESTAMP NOT NULL,
    status ENUM('PENDING','CONFIRMED','COMPLETED','CANCELLED','NO_SHOW') NOT NULL DEFAULT 'PENDING',
    note TEXT,
    cancelled_by BIGINT NULL,
    cancelled_reason TEXT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CHECK (start_datetime < end_datetime),
    CHECK (consultation_fee >= 0),
    
    -- INDEX: lấy lịch khám theo bệnh nhân
    INDEX idx_appt_patient_time (patient_id, start_datetime),
    -- INDEX: filter theo trạng thái 
    INDEX idx_appt_status (status),
    
    -- UNIQUE: chống double booking bác sĩ cùng 1 thời điểm
    UNIQUE KEY uq_doctor_time (doctor_id, start_datetime),
    FOREIGN KEY (doctor_id) REFERENCES doctors(id),
    FOREIGN KEY (patient_id) REFERENCES users(id),
    FOREIGN KEY (cancelled_by) REFERENCES users(id) ON DELETE SET NULL
);

-- APPOINTMENT STATUS HISTORY: lịch sử thay đổi trạng thái
CREATE TABLE appointment_status_histories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    appointment_id BIGINT NOT NULL,
    old_status ENUM('PENDING','CONFIRMED','COMPLETED','CANCELLED','NO_SHOW'),
    new_status ENUM('PENDING','CONFIRMED','COMPLETED','CANCELLED','NO_SHOW') NOT NULL,
    changed_by BIGINT NOT NULL,
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    -- INDEX: lấy lịch sử theo appointment
    INDEX idx_history_appt (appointment_id),
    FOREIGN KEY (appointment_id) REFERENCES appointments(id) ON DELETE CASCADE,
    FOREIGN KEY (changed_by) REFERENCES users(id)
);

-- MEDICAL RECORDS: hồ sơ bệnh án
CREATE TABLE medical_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    appointment_id BIGINT NOT NULL UNIQUE,
    diagnosis TEXT,
    prescription TEXT,
    note TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    
    -- INDEX: join appointment voi medical record
    INDEX idx_medical_appt (appointment_id),
    FOREIGN KEY (appointment_id) REFERENCES appointments(id) ON DELETE CASCADE
);

-- REFRESH TOKENS: JWT refresh token
CREATE TABLE refresh_tokens (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    token_hash VARCHAR(255) UNIQUE NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    revoked BOOLEAN DEFAULT FALSE,
    device_info VARCHAR(255),
    ip_address VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    -- INDEX: lấy token còn hạn, hết hạn 
    INDEX idx_refresh_expiry (expires_at),
    -- INDEX: lấy token theo user (login / refresh flow)
    INDEX idx_refresh_user (user_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- REVIEWS: đánh giá bác sĩ
CREATE TABLE reviews (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    appointment_id BIGINT NOT NULL UNIQUE,
    rating INT NOT NULL,
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    
    CHECK (rating BETWEEN 1 AND 5),
    FOREIGN KEY (appointment_id) REFERENCES appointments(id) ON DELETE CASCADE
);

-- AUDIT LOGS: log hệ thống (tracking thay đổi dữ liệu)
CREATE TABLE audit_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NULL,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id BIGINT,
    old_value TEXT,
    new_value TEXT,
    ip_address VARCHAR(50),
    user_agent VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    -- INDEX: query theo entity (audit theo bảng / record)
    INDEX idx_audit_entity (entity_type, entity_id),
    -- INDEX: query theo user
    INDEX idx_audit_user (user_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);