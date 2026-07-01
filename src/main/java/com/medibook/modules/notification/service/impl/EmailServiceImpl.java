package com.medibook.modules.notification.service.impl;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.medibook.modules.appointment.entity.Appointment;
import com.medibook.modules.notification.service.EmailService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendResetPasswordEmail(String email, String resetToken) {

        String resetUrl = "http://localhost:3000/reset-password?token=" + resetToken;

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(email);

        message.setSubject("MediBook Password Reset");

        message.setText(
                """
                        Hello,

                        We received a request to reset your password.

                        Click the link below:

                        %s

                        This link will expire in 15 minutes.

                        If you did not request this change,
                        please ignore this email.

                        MediBook Team
                        """.formatted(resetUrl));

        mailSender.send(message);
    }

    @Override
    public void sendVerificationEmail(String email, String token) {

        String verifyUrl = "http://localhost:3000/verify-email?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Verify your account");

        message.setText("""
                Welcome to MediBook!

                Please verify your account:

                %s

                This link expires in 15 minutes.
                """.formatted(verifyUrl));

        mailSender.send(message);
    }

    @Override
    @Async
    public void sendAppointmentCreatedEmail(Appointment appointment) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(appointment.getPatient().getEmail());

        message.setSubject("Appointment Confirmation");

        message.setText("""
                Hello %s,
                Your appointment has been booking successfully.

                Booking code: %s

                Doctor: %s

                Time: %s

                Thank you for using MediBook.
                """.formatted(appointment.getPatient().getFullName(), appointment.getBookingCode(),
                appointment.getDoctor().getUser().getFullName(), appointment.getStartDatetime()));

        mailSender.send(message);
    }

    @Override
    @Async
    public void sendAppointmentCancelledEmail(Appointment appointment) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(appointment.getPatient().getEmail());

        message.setSubject("Appointment Confirmation");

        message.setText("""
                Hello %s,
                Your appointment has been cancelled.

                Booking code: %s

                Doctor: %s

                Time: %s

                Please book another appointment if needed

                Medibook.
                """.formatted(appointment.getPatient().getFullName(), appointment.getBookingCode(),
                appointment.getDoctor().getUser().getFullName(), appointment.getStartDatetime()));

        mailSender.send(message);
    }
}
