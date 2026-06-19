package com.medibook.modules.schedule.dto.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TimeOffRequest {

    @NotNull
    private Long doctorId;

    @NotNull
    private LocalDateTime startDatetime;

    @NotNull
    private LocalDateTime endDatetime;

    @Size(max = 255)
    private String reason;
}
