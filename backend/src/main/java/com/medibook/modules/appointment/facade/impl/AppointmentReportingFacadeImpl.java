package com.medibook.modules.appointment.facade.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.medibook.common.enums.AppointmentStatus;
import com.medibook.modules.appointment.facade.AppointmentReportingFacade;
import com.medibook.modules.appointment.repository.StatusCountProjection;
import com.medibook.modules.appointment.service.AppointmentReportingService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppointmentReportingFacadeImpl implements AppointmentReportingFacade {

    private final AppointmentReportingService reportingService;

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
}