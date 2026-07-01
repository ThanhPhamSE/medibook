package com.medibook.modules.notification.service;

import com.medibook.modules.appointment.entity.Appointment;

public interface EmailService {

    void sendResetPasswordEmail(String email, String resetToken);

    void sendVerificationEmail(String email, String token);

    void sendAppointmentCreatedEmail(Appointment appointment);

    void sendAppointmentCancelledEmail(Appointment appointment);
}
