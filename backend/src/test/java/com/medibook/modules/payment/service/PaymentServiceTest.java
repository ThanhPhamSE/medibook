package com.medibook.modules.payment.service;

import com.medibook.common.enums.AppointmentStatus;
import com.medibook.common.exception.BadRequestException;
import com.medibook.common.exception.ResourceNotFoundException;
import com.medibook.modules.appointment.entity.Appointment;
import com.medibook.modules.appointment.repository.AppointmentRepository;
import com.medibook.modules.appointment.service.AppointmentService;
import com.medibook.modules.payment.dto.response.PaymentLinkResponse;
import com.medibook.modules.payment.entity.Payment;
import com.medibook.modules.payment.repository.PaymentRepository;
import com.medibook.modules.payment.service.impl.PaymentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import vn.payos.PayOS;
import vn.payos.service.blocking.v2.paymentRequests.PaymentRequestsService;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.webhooks.Webhook;
import vn.payos.model.webhooks.WebhookData;
import vn.payos.service.blocking.webhooks.WebhooksService;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private AppointmentService appointmentService;

    @Mock
    private PayOS payOS;

    @Mock
    private PaymentRequestsService paymentRequestsService;

    @Mock
    private WebhooksService webhooksService;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private Appointment testAppointment;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(paymentService, "frontendUrl", "http://localhost:3000");

        testAppointment = new Appointment();
        testAppointment.setId(1L);
        testAppointment.setBookingCode("BOOK123");
        testAppointment.setConsultationFee(BigDecimal.valueOf(150000));
        testAppointment.setStatus(AppointmentStatus.PENDING);
    }

    @Test
    void createPaymentLink_Success() throws Exception {
        when(appointmentRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(testAppointment));
        when(paymentRepository.findByAppointmentId(1L)).thenReturn(Optional.empty());
        when(paymentRepository.findByOrderCode(anyLong())).thenReturn(Optional.empty());

        CreatePaymentLinkResponse payOSResponse = mock(CreatePaymentLinkResponse.class);
        when(payOSResponse.getPaymentLinkId()).thenReturn("link-123");
        when(payOSResponse.getCheckoutUrl()).thenReturn("https://checkout.payos.vn/payment-link-details");

        when(payOS.paymentRequests()).thenReturn(paymentRequestsService);
        when(paymentRequestsService.create(any(CreatePaymentLinkRequest.class))).thenReturn(payOSResponse);

        PaymentLinkResponse response = paymentService.createPaymentLink(1L);

        assertThat(response).isNotNull();
        assertThat(response.getCheckoutUrl()).isEqualTo("https://checkout.payos.vn/payment-link-details");
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void createPaymentLink_AlreadyPaid_ThrowsBadRequest() {
        testAppointment.setStatus(AppointmentStatus.CONFIRMED);
        when(appointmentRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(testAppointment));

        assertThatThrownBy(() -> paymentService.createPaymentLink(1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already confirmed or completed");
    }

    @Test
    void handlePayOSWebhook_Success() throws Exception {
        Webhook webhook = mock(Webhook.class);
        WebhookData webhookData = mock(WebhookData.class);
        when(webhookData.getOrderCode()).thenReturn(123456789L);
        when(webhookData.getCode()).thenReturn("00");

        when(payOS.webhooks()).thenReturn(webhooksService);
        when(webhooksService.verify(webhook)).thenReturn(webhookData);

        Payment payment = new Payment();
        payment.setId(10L);
        payment.setAppointment(testAppointment);
        payment.setOrderCode(123456789L);
        payment.setStatus("PENDING");

        when(paymentRepository.findByOrderCode(123456789L)).thenReturn(Optional.of(payment));

        paymentService.handlePayOSWebhook(webhook);

        assertThat(payment.getStatus()).isEqualTo("PAID");
        verify(paymentRepository, times(1)).save(payment);
        verify(appointmentService, times(1)).confirmAppointmentPaid(testAppointment.getId());
    }
}
