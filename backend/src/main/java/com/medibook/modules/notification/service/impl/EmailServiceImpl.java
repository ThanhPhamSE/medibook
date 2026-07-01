package com.medibook.modules.notification.service.impl;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.medibook.config.AppProperties;
import com.medibook.modules.notification.dto.AppointmentEmailData;
import com.medibook.modules.notification.service.EmailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final AppProperties appProperties;

    @Override
    @Async("emailExecutor")
    public void sendResetPasswordEmail(String email, String resetToken) {

        String resetUrl = appProperties.getFrontendUrl() + "/reset-password?token=" + resetToken;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("MediBook Password Reset");
        message.setText("""
                Hello,

                We received a request to reset your password.

                Click the link below:

                %s

                This link will expire in 15 minutes.

                If you did not request this change,
                please ignore this email.

                MediBook Team
                """.formatted(resetUrl));

        try {
            mailSender.send(message);
            log.info("Password reset email sent to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", email, e);
        }
    }

    @Override
    @Async("emailExecutor")
    public void sendVerificationEmail(String email, String token) {

        String verifyUrl = appProperties.getFrontendUrl() + "/verify-email?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Verify your account");
        message.setText("""
                Welcome to MediBook!

                Please verify your account:

                %s

                This link expires in 15 minutes.
                """.formatted(verifyUrl));

        try {
            mailSender.send(message);
            log.info("Verification email sent to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", email, e);
        }
    }

    @Override
    @Async("emailExecutor")
    public void sendAppointmentCreatedEmail(AppointmentEmailData data) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(data.patientEmail());
        message.setSubject("Appointment Confirmation");
        message.setText("""
                Hello %s,
                Your appointment has been booked successfully.

                Booking code: %s

                Doctor: %s

                Time: %s

                Thank you for using MediBook.
                """.formatted(data.patientName(), data.bookingCode(),
                data.doctorName(), data.startDatetime()));

        try {
            mailSender.send(message);
            log.info("Appointment created email sent: bookingCode={}, patientEmail={}",
                    data.bookingCode(), data.patientEmail());
        } catch (Exception e) {
            log.error("Failed to send appointment created email: bookingCode={}", data.bookingCode(), e);
        }
    }

    @Override
    @Async("emailExecutor")
    public void sendAppointmentCancelledEmail(AppointmentEmailData data) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(data.patientEmail());
        message.setSubject("Appointment Cancellation");
        message.setText("""
                Hello %s,
                Your appointment has been cancelled.

                Booking code: %s

                Doctor: %s

                Time: %s

                Please book another appointment if needed.

                MediBook Team.
                """.formatted(data.patientName(), data.bookingCode(),
                data.doctorName(), data.startDatetime()));

        try {
            mailSender.send(message);
            log.info("Appointment cancelled email sent: bookingCode={}, patientEmail={}",
                    data.bookingCode(), data.patientEmail());
        } catch (Exception e) {
            log.error("Failed to send appointment cancelled email: bookingCode={}", data.bookingCode(), e);
        }
    }
}
