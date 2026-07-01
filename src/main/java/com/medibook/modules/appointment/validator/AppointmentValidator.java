package com.medibook.modules.appointment.validator;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.medibook.common.exception.BadRequestException;

@Component
public class AppointmentValidator {

    public void validateBookingTime(LocalDateTime startDateTime) {

        if (startDateTime == null) {
            throw new BadRequestException("Start time is required");
        }

        if (startDateTime.isBefore(LocalDateTime.now().plusHours(6))) {
            throw new BadRequestException("Appointment must be booked at least 6 hours advance");
        }
    }

    public void validatePatientOverlap(boolean overlap) {

        if (overlap) {
            throw new BadRequestException("You already have another appointment during this time");
        }
    }
}