package com.medibook.modules.schedule.dto.request;

import java.time.LocalTime;

import com.medibook.common.enums.DayOfWeekEnum;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WorkingPatternUpdateRequest {

    @NotNull
    private DayOfWeekEnum dayOfWeek;

    @NotNull
    private LocalTime startTime;

    @NotNull
    private LocalTime endTime;

    @NotNull
    private Integer slotDuration;

    private Integer bufferDuration;

}
