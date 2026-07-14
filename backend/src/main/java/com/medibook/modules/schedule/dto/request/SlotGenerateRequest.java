package com.medibook.modules.schedule.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SlotGenerateRequest {

    @NotNull
    private Long doctorId;

    @NotNull
    private LocalDate date;

}
