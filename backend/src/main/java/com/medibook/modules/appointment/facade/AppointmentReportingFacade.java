package com.medibook.modules.appointment.facade;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.medibook.common.enums.AppointmentStatus;
import com.medibook.modules.appointment.repository.StatusCountProjection;
import com.medibook.modules.reporting.dto.response.AppointmentTrendResponse;
import com.medibook.modules.reporting.dto.response.ChartPoint;
import com.medibook.modules.reporting.dto.response.RevenueTrendResponse;

public interface AppointmentReportingFacade {

    long countAppointments(LocalDateTime from, LocalDateTime to);

    long countAppointmentsByStatus(AppointmentStatus status, LocalDateTime from, LocalDateTime to);

    long countCompletedDoctor(Long doctorId);

    BigDecimal sumDoctorRevenue(Long doctorId);

    Long countCompletedAppointments(LocalDate from, LocalDate to);

    BigDecimal sumRevenue(LocalDate from, LocalDate to);

    List<StatusCountProjection> countGroupByStatus(LocalDateTime from, LocalDateTime to);

    List<AppointmentTrendResponse> getAppointmentTrend(LocalDateTime from, LocalDateTime to);

    List<RevenueTrendResponse> getRevenueTrend(LocalDate from, LocalDate to);

    List<ChartPoint> getSpecialtyDistribution();

    List<ChartPoint> getSpecialtyDistributionByAppointments();

    List<ChartPoint> getTopDoctors();
}
