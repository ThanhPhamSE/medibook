package com.medibook.modules.payment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentLinkResponse {
    private Long appointmentId;
    private Long orderCode;
    private BigDecimal amount;
    private String checkoutUrl;
    private String status;
}
