package com.medibook.modules.appointment.repository;

import java.time.LocalDate;

public interface AppointmentTrendProjection {

    LocalDate getDate();

    Long getTotal();

}
