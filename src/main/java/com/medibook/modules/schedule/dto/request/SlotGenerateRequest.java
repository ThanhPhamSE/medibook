package com.medibook.modules.schedule.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SlotGenerateRequest {

    @NotNull
    private Long doctorId;

    @NotNull
    private LocalDate date;

}
