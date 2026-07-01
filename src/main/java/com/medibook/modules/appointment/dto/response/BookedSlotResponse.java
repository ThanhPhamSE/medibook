package com.medibook.modules.appointment.dto.response;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookedSlotResponse {

    private LocalDateTime startDatetime;

    private LocalDateTime endDatetime;

}
