package com.medibook.modules.appointment.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.medibook.common.enums.AppointmentStatus;
import com.medibook.modules.appointment.repository.StatusCountProjection;

public interface AppointmentReportingService {

    long countAppointments(LocalDateTime from, LocalDateTime to);

    long countAppointmentsByStatus(AppointmentStatus status, LocalDateTime from, LocalDateTime to);

    long countCompletedDoctor(Long doctorId);

    BigDecimal sumDoctorRevenue(Long doctorId);

    Long countCompletedAppointments(LocalDate from, LocalDate to);

    BigDecimal sumRevenue(LocalDate from, LocalDate to);

    List<StatusCountProjection> countGroupByStatus(LocalDateTime from, LocalDateTime to);

}
