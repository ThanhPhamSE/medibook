package com.medibook.modules.appointment.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.medibook.common.enums.AppointmentStatus;
import com.medibook.modules.appointment.repository.AppointmentRepository;
import com.medibook.modules.appointment.repository.AppointmentTrendProjection;
import com.medibook.modules.appointment.repository.RevenueTrendProjection;
import com.medibook.modules.appointment.repository.SpecialtyDistributionProjection;
import com.medibook.modules.appointment.repository.StatusCountProjection;
import com.medibook.modules.appointment.service.AppointmentReportingService;
import com.medibook.modules.reporting.dto.response.ChartPoint;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AppointmentReportingServiceImpl implements AppointmentReportingService {

    private final AppointmentRepository appointmentRepository;

    @Override
    public long countAppointments(LocalDateTime from, LocalDateTime to) {
        return appointmentRepository.countByDeletedAtIsNullAndStartDatetimeBetween(from, to);
    }

    @Override
    public long countAppointmentsByStatus(AppointmentStatus status, LocalDateTime from, LocalDateTime to) {
        return appointmentRepository.countByDeletedAtIsNullAndStatusAndStartDatetimeBetween(status, from, to);
    }

    @Override
    public long countCompletedDoctor(Long doctorId) {
        return appointmentRepository.countByDeletedAtIsNullAndDoctorIdAndStatus(doctorId, AppointmentStatus.COMPLETED);
    }

    @Override
    public BigDecimal sumDoctorRevenue(Long doctorId) {
        return appointmentRepository.sumDoctorRevenue(doctorId, AppointmentStatus.COMPLETED);
    }

    @Override
    public Long countCompletedAppointments(LocalDate from, LocalDate to) {
        return appointmentRepository.countByDeletedAtIsNullAndStatusAndStartDatetimeBetween(AppointmentStatus.COMPLETED,
                from.atStartOfDay(), to.plusDays(1).atStartOfDay());
    }

    @Override
    public BigDecimal sumRevenue(LocalDate from, LocalDate to) {
        return appointmentRepository.sumRevenue(
                from.atStartOfDay(), to.plusDays(1).atStartOfDay(), AppointmentStatus.COMPLETED);
    }

    @Override
    public List<StatusCountProjection> countGroupByStatus(LocalDateTime from, LocalDateTime to) {
        return appointmentRepository.countGroupByStatus(from, to);
    }

    @Override
    public List<AppointmentTrendProjection> getAppointmentTrend(LocalDateTime from, LocalDateTime to) {
        return appointmentRepository.getAppointmentTrend(from, to);
    }

    @Override
    public List<RevenueTrendProjection> getRevenueTrend(LocalDateTime from, LocalDateTime to) {
        return appointmentRepository.getRevenueTrend(from, to, AppointmentStatus.COMPLETED);
    }

    @Override
    public List<ChartPoint> getSpecialtyDistribution() {
        return appointmentRepository.getSpecialtyDistribution()
                .stream()
                .map(sd -> ChartPoint.builder()
                        .label(sd.getLabel())
                        .value(sd.getValue() != null ? sd.getValue() : 0L)
                        .build())
                .toList();
    }
}
