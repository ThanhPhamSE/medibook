package com.medibook.modules.notification.service;

public interface EmailService {

    void sendResetPasswordEmail(String email, String resetToken);

    void sendVerificationEmail(String email, String token);
}
