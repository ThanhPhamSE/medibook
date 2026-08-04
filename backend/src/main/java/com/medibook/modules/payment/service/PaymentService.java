package com.medibook.modules.payment.service;

import com.medibook.modules.payment.dto.response.PaymentLinkResponse;
import vn.payos.model.webhooks.Webhook;

public interface PaymentService {
    PaymentLinkResponse createPaymentLink(Long appointmentId);
    void handlePayOSWebhook(Webhook webhookBody);
    PaymentLinkResponse getPaymentStatus(Long appointmentId);
    PaymentLinkResponse verifyAndConfirmPayment(Long appointmentId);
}

