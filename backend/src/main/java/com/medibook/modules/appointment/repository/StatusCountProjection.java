package com.medibook.modules.appointment.repository;

import com.medibook.common.enums.AppointmentStatus;

/**
 * DTO Projection cho GROUP BY status query.
 * Tránh load toàn bộ Appointment entity khi chỉ cần count theo status.
 */
public interface StatusCountProjection {

    AppointmentStatus getStatus();

    Long getCnt();
}
