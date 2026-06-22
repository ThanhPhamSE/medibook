package com.medibook.modules.appointment.validator;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.medibook.common.exception.BadRequestException;
import com.medibook.modules.doctor.entity.Doctor;
import com.medibook.modules.user.entity.User;

import com.medibook.modules.appointment.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AppointmentValidator {

    private final AppointmentRepository appointmentRepository;

    public void validateBookingTime(LocalDateTime startDateTime) {

        if (startDateTime == null) {
            throw new BadRequestException("Start time is required");
        }

        if (startDateTime.isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Cannot book appointment in the past");
        }
    }

    public void validateDoctor(Doctor doctor) {

        if (doctor == null) {
            throw new BadRequestException("Doctor not found");
        }

        if (doctor.getDeletedAt() != null) {
            throw new BadRequestException("Doctor is deleted");
        }

        if (doctor.getUser() == null || !doctor.getUser().getIsActive()) {
            throw new BadRequestException("Doctor is inactive");
        }
    }

    public void validatePatient(User patient) {

        if (patient == null || !patient.getIsActive()) {
            throw new BadRequestException("Account is inactive");
        }
    }

    public void validatePatientConflict(Long patientId, LocalDateTime startDateTime) {

        boolean exists = appointmentRepository.existsByPatientIdAndStartDatetimeAndDeletedAtIsNull(patientId,
                startDateTime);

        if (exists) {
            throw new BadRequestException("You already have an appointment at this time");
        }
    }

}