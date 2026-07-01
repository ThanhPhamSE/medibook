package com.medibook.modules.appointment.port;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

public interface AppointmentSchedulePort {

    boolean hasFutureAppointments(Long doctorId, LocalDateTime from);

    Set<LocalDateTime> getBookedSlots(Long doctorId, LocalDate date);
}
