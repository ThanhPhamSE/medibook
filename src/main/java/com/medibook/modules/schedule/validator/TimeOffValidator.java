package com.medibook.modules.schedule.validator;

import org.springframework.stereotype.Component;

import com.medibook.common.exception.BadRequestException;
import com.medibook.modules.schedule.dto.request.TimeOffRequest;

@Component
public class TimeOffValidator {

    public void validate(TimeOffRequest request) {

        if (request.getStartDatetime().isAfter(request.getEndDatetime())) {
            throw new BadRequestException("Invalid time range");
        }

        if (request.getStartDatetime().equals(request.getEndDatetime())) {
            throw new BadRequestException("Time off cannot be zero duraiton");
        }
    }
}
