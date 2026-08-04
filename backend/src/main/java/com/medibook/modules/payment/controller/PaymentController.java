package com.medibook.modules.payment.controller;

import com.medibook.modules.payment.dto.response.PaymentLinkResponse;
import com.medibook.modules.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.payos.model.webhooks.Webhook;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payment Management", description = "Endpoints for PayOS payment integration")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create-link/{appointmentId}")
    @Operation(summary = "Create PayOS payment link for an appointment")
    public ResponseEntity<PaymentLinkResponse> createPaymentLink(@PathVariable Long appointmentId) {
        PaymentLinkResponse response = paymentService.createPaymentLink(appointmentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{appointmentId}")
    @Operation(summary = "Get payment status for an appointment from DB")
    public ResponseEntity<PaymentLinkResponse> getPaymentStatus(@PathVariable Long appointmentId) {
        PaymentLinkResponse response = paymentService.getPaymentStatus(appointmentId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify/{appointmentId}")
    @Operation(summary = "Verify payment with PayOS and confirm appointment if paid (for dev without webhook)")
    public ResponseEntity<PaymentLinkResponse> verifyAndConfirmPayment(@PathVariable Long appointmentId) {
        PaymentLinkResponse response = paymentService.verifyAndConfirmPayment(appointmentId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/payos-webhook")
    @Operation(summary = "Webhook receiver for PayOS transaction notifications")
    public ResponseEntity<String> handlePayOSWebhook(@RequestBody Webhook webhookBody) {
        paymentService.handlePayOSWebhook(webhookBody);
        return ResponseEntity.ok("Webhook processed successfully");
    }
}
