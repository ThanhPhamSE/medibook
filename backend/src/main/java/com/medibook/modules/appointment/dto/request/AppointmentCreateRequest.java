package com.medibook.modules.appointment.dto.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppointmentCreateRequest {

    @NotNull
    private Long doctorId;

    @NotNull
    private LocalDateTime startDateTime;

    @Size(max = 500)
    private String note;
}
