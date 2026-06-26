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

        Long patientUserId = medicalRecord.getAppointment().getPatient().getId();
        Long doctorUserId = medicalRecord.getAppointment().getDoctor().getUser().getId();

        if (!currentUser.getId().equals(patientUserId) && !currentUser.getId().equals(doctorUserId)) {
            throw new ForbiddenException("Access denied");
        }
    }

}
