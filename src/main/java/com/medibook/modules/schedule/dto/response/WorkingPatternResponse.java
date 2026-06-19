package com.medibook.modules.schedule.dto.response;

import java.time.LocalTime;

import com.medibook.common.enums.DayOfWeekEnum;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WorkingPatternResponse {

    private Long id;

    private Long doctorId;

    private DayOfWeekEnum dayOfWeek;

    private LocalTime startTime;

    private LocalTime endTime;

    private Integer slotDuration;

    private Integer bufferDuration;

}
