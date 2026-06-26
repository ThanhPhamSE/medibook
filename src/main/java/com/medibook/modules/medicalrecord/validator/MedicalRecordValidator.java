package com.medibook.modules.medicalrecord.validator;

import org.springframework.stereotype.Component;

import com.medibook.common.enums.AppointmentStatus;
import com.medibook.common.exception.BadRequestException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MedicalRecordValidator {

    public void validateCreate(Long appointmentId, AppointmentStatus status, boolean exists) {

        if (status != AppointmentStatus.COMPLETED) {
            throw new BadRequestException("Medical record can only be created for completed appointment");
        }

        if (exists) {
            throw new BadRequestException("Medical record already exists");
        }
    }
}
