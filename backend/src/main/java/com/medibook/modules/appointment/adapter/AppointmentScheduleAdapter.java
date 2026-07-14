package com.medibook.modules.appointment.adapter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.medibook.modules.appointment.entity.Appointment;
import com.medibook.modules.appointment.port.AppointmentSchedulePort;
import com.medibook.modules.appointment.repository.AppointmentRepository;

@Component
public class AppointmentScheduleAdapter implements AppointmentSchedulePort {

    private final AppointmentRepository appointmentRepository;

    public AppointmentScheduleAdapter(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    @Override
    public boolean hasFutureAppointments(Long doctorId, LocalDateTime from) {
        return appointmentRepository.existsFutureAppointment(doctorId, from);
    }

    @Override
    public Set<LocalDateTime> getBookedSlots(Long doctorId, LocalDate date) {
        return appointmentRepository.findActiveAppointmentsByDate(
                doctorId,
                date.atStartOfDay(),
                date.plusDays(1).atStartOfDay()).stream()
                .map(Appointment::getStartDatetime)
                .collect(java.util.stream.Collectors.toSet());
    }
}
