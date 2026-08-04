package com.medibook.modules.payment.service.impl;

import com.medibook.common.exception.BadRequestException;
import com.medibook.common.exception.ResourceNotFoundException;
import com.medibook.modules.appointment.entity.Appointment;
import com.medibook.modules.appointment.repository.AppointmentRepository;
import com.medibook.modules.appointment.service.AppointmentService;
import com.medibook.modules.payment.dto.response.PaymentLinkResponse;
import com.medibook.modules.payment.entity.Payment;
import com.medibook.modules.payment.repository.PaymentRepository;
import com.medibook.modules.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.payos.PayOS;
import vn.payos.model.webhooks.Webhook;
import vn.payos.model.webhooks.WebhookData;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLink;
import vn.payos.model.v2.paymentRequests.PaymentLinkStatus;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final AppointmentRepository appointmentRepository;
    private final AppointmentService appointmentService;
    private final PayOS payOS;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    @Transactional
    public PaymentLinkResponse createPaymentLink(Long appointmentId) {
        Appointment appointment = appointmentRepository.findByIdAndDeletedAtIsNull(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        if (com.medibook.common.enums.AppointmentStatus.CONFIRMED.equals(appointment.getStatus()) ||
            com.medibook.common.enums.AppointmentStatus.COMPLETED.equals(appointment.getStatus())) {
            throw new BadRequestException("Appointment is already confirmed or completed");
        }

        Payment existingPayment = paymentRepository.findByAppointmentId(appointmentId).orElse(null);
        if (existingPayment != null && "PAID".equals(existingPayment.getStatus())) {
            throw new BadRequestException("Appointment is already paid");
        }

        long orderCode;
        do {
            orderCode = System.currentTimeMillis() + (long) (Math.random() * 10000);
        } while (paymentRepository.findByOrderCode(orderCode).isPresent());

        long amount = appointment.getConsultationFee().longValue();
        String description = "MediBook Appt " + appointment.getBookingCode();
        String returnUrl = frontendUrl + "/payment-success?appointmentId=" + appointmentId;
        String cancelUrl = frontendUrl + "/payment-cancel?appointmentId=" + appointmentId;

        CreatePaymentLinkRequest checkoutRequestData = CreatePaymentLinkRequest.builder()
                .orderCode(orderCode)
                .amount(amount)
                .description(description)
                .returnUrl(returnUrl)
                .cancelUrl(cancelUrl)
                .build();

        try {
            CreatePaymentLinkResponse paymentLinkData = payOS.paymentRequests().create(checkoutRequestData);

            Payment payment = existingPayment != null ? existingPayment : new Payment();
            payment.setAppointment(appointment);
            payment.setOrderCode(orderCode);
            payment.setAmount(appointment.getConsultationFee());
            payment.setStatus("PENDING");
            payment.setPaymentLinkId(paymentLinkData.getPaymentLinkId());
            payment.setCheckoutUrl(paymentLinkData.getCheckoutUrl());

            paymentRepository.save(payment);

            return PaymentLinkResponse.builder()
                    .appointmentId(appointmentId)
                    .orderCode(orderCode)
                    .amount(appointment.getConsultationFee())
                    .checkoutUrl(paymentLinkData.getCheckoutUrl())
                    .status("PENDING")
                    .build();
        } catch (Exception e) {
            log.error("Failed to create PayOS payment link for appointmentId={}", appointmentId, e);
            throw new BadRequestException("Failed to generate payment link: " + e.getMessage());
        }
    }

    @Override
    public PaymentLinkResponse getPaymentStatus(Long appointmentId) {
        Payment payment = paymentRepository.findByAppointmentId(appointmentId).orElse(null);
        if (payment == null) {
            return PaymentLinkResponse.builder()
                    .appointmentId(appointmentId)
                    .status("NOT_FOUND")
                    .build();
        }
        return PaymentLinkResponse.builder()
                .appointmentId(appointmentId)
                .orderCode(payment.getOrderCode())
                .amount(payment.getAmount())
                .checkoutUrl(payment.getCheckoutUrl())
                .status(payment.getStatus())
                .build();
    }

    /**
     * Gọi PayOS API để lấy trạng thái payment hiện tại, rồi tự update DB nếu đã thanh toán.
     * Dùng khi webhook không reach được localhost (dev environment).
     */
    @Override
    @Transactional
    public PaymentLinkResponse verifyAndConfirmPayment(Long appointmentId) {
        Payment payment = paymentRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for appointmentId: " + appointmentId));

        // Nếu đã PAID trong DB rồi thì trả về luôn
        if ("PAID".equals(payment.getStatus())) {
            return PaymentLinkResponse.builder()
                    .appointmentId(appointmentId)
                    .orderCode(payment.getOrderCode())
                    .amount(payment.getAmount())
                    .status("PAID")
                    .build();
        }

        // Retry tối đa 5 lần, mỗi lần cách nhau 2 giây
        // PayOS đôi khi trả về PENDING hoặc PROCESSING ngay sau redirect
        int maxRetries = 5;
        int retryDelayMs = 2000;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                PaymentLink paymentLink = payOS.paymentRequests().get(payment.getOrderCode());
                PaymentLinkStatus payosStatus = paymentLink.getStatus();

                log.info("PayOS status check attempt={} orderCode={}: {}", attempt, payment.getOrderCode(), payosStatus);

                if (PaymentLinkStatus.PAID.equals(payosStatus)) {
                    payment.setStatus("PAID");
                    paymentRepository.save(payment);
                    appointmentService.confirmAppointmentPaid(appointmentId);
                    log.info("Payment verified and appointment confirmed for appointmentId={}", appointmentId);
                    return PaymentLinkResponse.builder()
                            .appointmentId(appointmentId)
                            .orderCode(payment.getOrderCode())
                            .amount(payment.getAmount())
                            .status("PAID")
                            .build();
                } else if (PaymentLinkStatus.CANCELLED.equals(payosStatus)
                        || PaymentLinkStatus.EXPIRED.equals(payosStatus)
                        || PaymentLinkStatus.FAILED.equals(payosStatus)) {
                    payment.setStatus("CANCELLED");
                    paymentRepository.save(payment);
                    return PaymentLinkResponse.builder()
                            .appointmentId(appointmentId)
                            .orderCode(payment.getOrderCode())
                            .amount(payment.getAmount())
                            .status("CANCELLED")
                            .build();
                }
                // PENDING / PROCESSING / UNDERPAID -> thử lại sau
                if (attempt < maxRetries) {
                    Thread.sleep(retryDelayMs);
                }
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.warn("Attempt {} failed to query PayOS for orderCode={}: {}", attempt, payment.getOrderCode(), e.getMessage());
                if (attempt < maxRetries) {
                    try { Thread.sleep(retryDelayMs); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); break; }
                }
            }
        }

        // Hết retry mà chưa PAID — trả về status hiện tại trong DB
        log.warn("Could not confirm PAID status after {} retries for appointmentId={}", maxRetries, appointmentId);
        return PaymentLinkResponse.builder()
                .appointmentId(appointmentId)
                .orderCode(payment.getOrderCode())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .build();
    }

    @Override
    @Transactional
    public void handlePayOSWebhook(Webhook webhookBody) {
        try {
            WebhookData verifiedData = payOS.webhooks().verify(webhookBody);
            Long orderCode = verifiedData.getOrderCode();

            Payment payment = paymentRepository.findByOrderCode(orderCode)
                    .orElseThrow(() -> new ResourceNotFoundException("Payment not found for orderCode: " + orderCode));

            if ("PAID".equals(payment.getStatus())) {
                log.info("Payment with orderCode={} was already processed", orderCode);
                return;
            }

            // PayOS code: "00" represents success
            if ("00".equals(verifiedData.getCode())) {
                payment.setStatus("PAID");
                paymentRepository.save(payment);

                appointmentService.confirmAppointmentPaid(payment.getAppointment().getId());
                log.info("Successfully updated payment and confirmed appointment for orderCode={}", orderCode);
            } else {
                payment.setStatus("CANCELLED");
                paymentRepository.save(payment);
                log.warn("Payment with orderCode={} failed or was cancelled with code={}", orderCode, verifiedData.getCode());
            }
        } catch (Exception e) {
            log.error("Error processing PayOS webhook", e);
            throw new BadRequestException("Invalid webhook data or verification failed: " + e.getMessage());
        }
    }
}
