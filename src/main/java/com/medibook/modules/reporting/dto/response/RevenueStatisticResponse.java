package com.medibook.modules.reporting.dto.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueStatisticResponse {

    private Long totalAppointments;

    private BigDecimal totalRevenue;

}
