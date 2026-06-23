package com.medibook.modules.medicalrecord.validator;

import org.springframework.stereotype.Component;

import com.medibook.common.exception.ForbiddenException;
import com.medibook.modules.appointment.entity.Appointment;
import com.medibook.modules.medicalrecord.entity.MedicalRecord;
import com.medibook.modules.user.entity.User;

@Component
public class MedicalRecordSecurityValidator {

    public void validateDoctorOwnership(Appointment appointment, User currentUser) {

        if (!appointment.getDoctor().getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You are not assigned to this appointment");
        }

    }

    public void validateViewPermission(MedicalRecord medicalRecord, User currentUser, boolean isAdmin) {

        if (isAdmin) {
            return;
        }

        Long patientId = medicalRecord.getAppointment().getPatient().getId();

        Long doctorId = medicalRecord.getAppointment().getDoctor().getId();

        if (!currentUser.getId().equals(patientId) && !currentUser.getId().equals(doctorId)) {
            throw new ForbiddenException("Access denied");
        }
    }

}
