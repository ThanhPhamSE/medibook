package com.medibook.modules.reporting.dto.response;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardResponse {

    private long totalPatients;
    private long totalDoctors;
    private long totalAppointments;
    private BigDecimal totalRevenue;

    private List<AppointmentTrendResponse> appointmentTrend;
    private List<RevenueTrendResponse> revenueTrend;
    private List<SpecialtyDistributionResponse> specialtyDistribution;
    private List<TopDoctorResponse> topDoctors;

}
