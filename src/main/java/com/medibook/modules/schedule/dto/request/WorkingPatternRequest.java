package com.medibook.modules.schedule.dto.request;

import java.time.LocalTime;

import com.medibook.common.enums.DayOfWeekEnum;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WorkingPatternRequest {

    @NotNull
    private Long doctorId;

    @NotNull
    private DayOfWeekEnum dayOfWeek;

    @NotNull
    private LocalTime startTime;

    @NotNull
    private LocalTime endTime;

    @NotNull
    @Min(10)
    private Integer slotDuration;

    private Integer bufferDuration = 0;
}
