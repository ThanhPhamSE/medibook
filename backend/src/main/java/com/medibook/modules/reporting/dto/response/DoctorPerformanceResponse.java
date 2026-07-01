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
public class DoctorPerformanceResponse {

    private Long doctorId;

    private Long completedAppointments;

    private BigDecimal revenue;

    private Double averageRating;

}
