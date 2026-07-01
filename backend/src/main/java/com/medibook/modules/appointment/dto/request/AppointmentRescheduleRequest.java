package com.medibook.modules.appointment.dto.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppointmentRescheduleRequest {

    @NotNull
    private LocalDateTime newStartDatetime;

}
