package com.medibook.modules.appointment.facade.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.medibook.common.enums.AppointmentStatus;
import com.medibook.modules.appointment.facade.AppointmentReportingFacade;
import com.medibook.modules.appointment.repository.StatusCountProjection;
import com.medibook.modules.appointment.service.AppointmentReportingService;
import com.medibook.modules.reporting.dto.response.AppointmentTrendResponse;
import com.medibook.modules.reporting.dto.response.ChartPoint;
import com.medibook.modules.reporting.dto.response.RevenueTrendResponse;
import com.medibook.modules.doctor.dto.request.DoctorSearchRequest;
import com.medibook.modules.doctor.service.DoctorService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppointmentReportingFacadeImpl implements AppointmentReportingFacade {

    private final AppointmentReportingService reportingService;
    private final DoctorService doctorService;

    @Override
    public long countAppointments(LocalDateTime from, LocalDateTime to) {
        return reportingService.countAppointments(from, to);
    }

    @Override
    public long countAppointmentsByStatus(AppointmentStatus status, LocalDateTime from, LocalDateTime to) {
        return reportingService.countAppointmentsByStatus(status, from, to);
    }

    @Override
    public long countCompletedDoctor(Long doctorId) {
        return reportingService.countCompletedDoctor(doctorId);
    }

    @Override
    public BigDecimal sumDoctorRevenue(Long doctorId) {
        return reportingService.sumDoctorRevenue(doctorId);
    }

    @Override
    public Long countCompletedAppointments(LocalDate from, LocalDate to) {
        return reportingService.countCompletedAppointments(from, to);
    }

    @Override
    public BigDecimal sumRevenue(LocalDate from, LocalDate to) {
        return reportingService.sumRevenue(from, to);
    }

    @Override
    public List<StatusCountProjection> countGroupByStatus(LocalDateTime from, LocalDateTime to) {
        return reportingService.countGroupByStatus(from, to);
    }

    @Override
    public List<AppointmentTrendResponse> getAppointmentTrend(LocalDateTime from, LocalDateTime to) {

        return reportingService.getAppointmentTrend(from, to).stream()
                .map(item -> AppointmentTrendResponse.builder().date(item.getDate()).total(item.getTotal()).build())
                .toList();
    }

    @Override
    public List<RevenueTrendResponse> getRevenueTrend(LocalDate from, LocalDate to) {
        return reportingService.getRevenueTrend(from.atStartOfDay(), to.plusDays(1).atStartOfDay()).stream()
                .map(item -> RevenueTrendResponse.builder()
                        .date(item.getDate())
                        .total(item.getTotal())
                        .build())
                .toList();
    }

    @Override
    public List<ChartPoint> getSpecialtyDistribution() {
        return getSpecialtyDistributionByAppointments();
    }

    @Override
    public List<ChartPoint> getSpecialtyDistributionByAppointments() {
        return reportingService.getSpecialtyDistribution();
    }

    @Override
    public List<ChartPoint> getTopDoctors() {
        return doctorService.searchDoctors(
                new DoctorSearchRequest(),
                PageRequest.of(0, 10)).getItems()
                .stream()
                .map(d -> ChartPoint.builder()
                        .label(d.getFullName())
                        .value(d.getTotalReviews() != null ? d.getTotalReviews() : 0L)
                        .build())
                .toList();
    }
}
