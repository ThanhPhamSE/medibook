package com.medibook.modules.appointment.adapter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.medibook.modules.appointment.dto.response.BookedSlotResponse;
import com.medibook.modules.appointment.port.AppointmentSchedulePort;
import com.medibook.modules.appointment.repository.AppointmentRepository;
import com.medibook.modules.appointment.service.AppointmentService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AppointmentScheduleAdapter implements AppointmentSchedulePort {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentService appointmentService;

    @Override
    public boolean hasFutureAppointments(Long doctorId, LocalDateTime from) {
        return appointmentRepository.existsFutureAppointment(doctorId, from);
    }

    @Override
    public Set<LocalDateTime> getBookedSlots(Long doctorId, LocalDate date) {

        return appointmentService.getBookedAppointmentsByDate(doctorId, date)
                .stream()
                .map(BookedSlotResponse::getStartDatetime)
                .collect(Collectors.toSet());
    }
}
