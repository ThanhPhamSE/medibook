package com.medibook.modules.appointment.validator;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.medibook.common.enums.AppointmentStatus;
import com.medibook.common.exception.BadRequestException;
import com.medibook.modules.doctor.entity.Doctor;
import com.medibook.modules.user.entity.User;
import com.medibook.modules.appointment.entity.Appointment;
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

        if (startDateTime.isBefore(LocalDateTime.now().plusHours(6))) {
            throw new BadRequestException("Appointment must be booked at least 6 hours advance");
        }
    }

    public void validateTransition(Appointment appointment, AppointmentStatus targetStatus) {

        AppointmentStatus current = appointment.getStatus();

        boolean valid = (current == AppointmentStatus.PENDING && targetStatus == AppointmentStatus.CONFIRMED)
                || (current == AppointmentStatus.PENDING && targetStatus == AppointmentStatus.CANCELLED)
                || (current == AppointmentStatus.CONFIRMED && targetStatus == AppointmentStatus.COMPLETED)
                || (current == AppointmentStatus.CONFIRMED && targetStatus == AppointmentStatus.NO_SHOW)
                || (current == AppointmentStatus.CONFIRMED && targetStatus == AppointmentStatus.CANCELLED);

        if (!valid) {
            throw new BadRequestException("Invalid appointment status transition");
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

    public void validateAppointmentOwner(Appointment appointment, User currentUser) {

        if (!appointment.getPatient().getId().equals(currentUser.getId())) {
            throw new BadRequestException("You do not have permission to access this appointment");
        }
    }

    public void validateCancelable(Appointment appointment) {

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new BadRequestException("Appointment already cancelled");
        }

        if (appointment.getStatus() != AppointmentStatus.PENDING
                && appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new BadRequestException("Appointment cannot be cancelled");
        }
        if (appointment.getStartDatetime().isBefore(LocalDateTime.now().plusHours(24))) {

            throw new BadRequestException("Appointment can only be cancelled at least 24 hours in advance");
        }
    }

    public void validatePatientOverlap(Long patientId, Long appointmentId, LocalDateTime startTime,
            LocalDateTime endTime) {

        if (appointmentRepository.existsPatientOverlap(patientId, appointmentId, startTime, endTime)) {
            throw new BadRequestException("You already have another appointment during this time ");
        }
    }
}