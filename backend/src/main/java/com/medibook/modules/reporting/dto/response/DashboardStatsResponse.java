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
public class DashboardStatsResponse {

    private long totalPatients;

    private long totalDoctors;

    private long totalAppointments;

    private BigDecimal totalRevenue;

}
