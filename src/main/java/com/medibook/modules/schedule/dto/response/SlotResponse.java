package com.medibook.modules.schedule.dto.response;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SlotResponse {

    private LocalDateTime start;

    private LocalDateTime end;

    private boolean available;
}
