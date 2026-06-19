package com.medibook.modules.schedule.validator;

import java.time.Duration;

import org.springframework.stereotype.Component;

import com.medibook.common.exception.BadRequestException;
import com.medibook.modules.schedule.dto.request.WorkingPatternRequest;

@Component
public class WorkingPatternValidator {

    public void validate(WorkingPatternRequest request) {

        if (request.getBufferDuration() == null) {
            request.setBufferDuration(0);
        }

        if (request.getBufferDuration() < 0) {
            throw new BadRequestException("Buffer duration must be >= 0");
        }

        if (request.getStartTime().isAfter(request.getEndTime())) {
            throw new BadRequestException("Start time must me before end time");
        }

        long duration = Duration.between(request.getStartTime(), request.getEndTime()).toMinutes();

        if (duration < request.getSlotDuration()) {
            throw new BadRequestException("Working time too short for slot duration");
        }

        if (duration % request.getSlotDuration() != 0) {
            throw new BadRequestException("Working time must divide evenly into slots");
        }
    }

}
