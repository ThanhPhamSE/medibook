package com.medibook.modules.schedule.dto.response;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TimeOffResponse {

    private Long id;

    private Long doctorId;

    private LocalDateTime startDateTime;

    private LocalDateTime endDateTime;

    private String reason;
}
