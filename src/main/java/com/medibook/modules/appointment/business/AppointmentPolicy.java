package com.medibook.modules.appointment.business;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.medibook.common.exception.BadRequestException;
import com.medibook.modules.schedule.entity.DoctorWorkingPattern;

@Component
public class AppointmentPolicy {

    public void validateSlot(LocalDateTime start, DoctorWorkingPattern pattern) {

        LocalDateTime end = start.plusMinutes(pattern.getSlotDuration());

        if (start.toLocalTime().isBefore(pattern.getStartTime()) || end.toLocalTime().isAfter(pattern.getEndTime())) {
            throw new BadRequestException("Outside working hours");
        }

        int interval = pattern.getSlotDuration()
                + (pattern.getBufferDuration() == null ? 0 : pattern.getBufferDuration());

        long diff = java.time.Duration.between(pattern.getStartTime(), start.toLocalTime()).toMinutes();

        if (diff % interval != 0) {
            throw new BadRequestException("Invalid slot alignment");
        }
    }

}
