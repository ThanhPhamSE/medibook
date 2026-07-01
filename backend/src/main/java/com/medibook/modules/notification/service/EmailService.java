package com.medibook.modules.notification.service;

import com.medibook.modules.notification.dto.AppointmentEmailData;

public interface EmailService {

    void sendResetPasswordEmail(String email, String resetToken);

    void sendVerificationEmail(String email, String token);

    void sendAppointmentCreatedEmail(AppointmentEmailData data);

    void sendAppointmentCancelledEmail(AppointmentEmailData data);
}

