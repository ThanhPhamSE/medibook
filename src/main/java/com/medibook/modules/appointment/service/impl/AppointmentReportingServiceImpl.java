package com.medibook.modules.appointment.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.medibook.common.enums.AppointmentStatus;
import com.medibook.modules.appointment.repository.AppointmentRepository;
import com.medibook.modules.appointment.service.AppointmentReportingService;

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
        return appointmentRepository.sumDoctorRevenue(doctorId);
    }

    @Override
    public Long countCompletedAppointments(LocalDate from, LocalDate to) {
        return appointmentRepository.countByDeletedAtIsNullAndStatusAndStartDatetimeBetween(AppointmentStatus.COMPLETED,
                from.atStartOfDay(), to.plusDays(1).atStartOfDay());
    }

    @Override
    public BigDecimal sumRevenue(LocalDate from, LocalDate to) {
        return appointmentRepository.sumRevenue(from.atStartOfDay(), to.plusDays(1).atStartOfDay());
    }
}
