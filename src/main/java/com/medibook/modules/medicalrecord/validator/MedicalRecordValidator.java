package com.medibook.modules.medicalrecord.validator;

import org.springframework.stereotype.Component;

import com.medibook.common.enums.AppointmentStatus;
import com.medibook.common.exception.BadRequestException;
import com.medibook.modules.appointment.entity.Appointment;
import com.medibook.modules.medicalrecord.repository.MedicalRecordRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MedicalRecordValidator {

    private final MedicalRecordRepository medicalRecordRepository;

    public void validateCreate(Appointment appointment) {

        if (appointment.getStatus() != AppointmentStatus.COMPLETED) {
            throw new BadRequestException("Medical record can only be created for completed appointment");
        }

        if (medicalRecordRepository.existsByAppointmentId(appointment.getId())) {
            throw new BadRequestException("Medical record already exists");
        }
    }
}
