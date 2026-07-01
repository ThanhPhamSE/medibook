package com.medibook.modules.notification.dto;

import java.time.LocalDateTime;

/**
 * Lightweight data record truyền thông tin appointment cần thiết cho email.
 * Tách coupling giữa notification module và appointment Entity,
 * đồng thời tránh LazyInitializationException khi EmailService chạy trong @Async thread.
 */
public record AppointmentEmailData(
        String patientEmail,
        String patientName,
        String bookingCode,
        String doctorName,
        LocalDateTime startDatetime
) {

    public static AppointmentEmailData from(
            com.medibook.modules.appointment.entity.Appointment appointment) {
        return new AppointmentEmailData(
                appointment.getPatient().getEmail(),
                appointment.getPatient().getFullName(),
                appointment.getBookingCode(),
                appointment.getDoctor().getUser().getFullName(),
                appointment.getStartDatetime()
        );
    }
}
