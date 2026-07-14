package com.medibook.modules.appointment.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.medibook.common.enums.AppointmentStatus;
import com.medibook.modules.appointment.repository.AppointmentTrendProjection;
import com.medibook.modules.appointment.repository.RevenueTrendProjection;
import com.medibook.modules.appointment.repository.StatusCountProjection;
import com.medibook.modules.reporting.dto.response.ChartPoint;

public interface AppointmentReportingService {

    long countAppointments(LocalDateTime from, LocalDateTime to);

    long countAppointmentsByStatus(AppointmentStatus status, LocalDateTime from, LocalDateTime to);

    long countCompletedDoctor(Long doctorId);

    BigDecimal sumDoctorRevenue(Long doctorId);

    Long countCompletedAppointments(LocalDate from, LocalDate to);

    BigDecimal sumRevenue(LocalDate from, LocalDate to);

    List<StatusCountProjection> countGroupByStatus(LocalDateTime from, LocalDateTime to);

    List<AppointmentTrendProjection> getAppointmentTrend(LocalDateTime from, LocalDateTime to);

    List<RevenueTrendProjection> getRevenueTrend(LocalDateTime from, LocalDateTime to);

    List<ChartPoint> getSpecialtyDistribution();
}
