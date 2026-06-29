package com.medibook.modules.medicalrecord.validator;

import org.springframework.stereotype.Component;

import com.medibook.common.exception.ForbiddenException;

@Component
public class MedicalRecordSecurityValidator {

    public void validateDoctorOwnership(Long doctorUserId, Long currentUserId) {

        if (!doctorUserId.equals(currentUserId)) {
            throw new ForbiddenException("You are not assigned to this appointment");
        }

    }

    public void validateViewPermission(Long patientUserId, Long doctorUserId, Long currentUserId, boolean isAdmin) {

        if (isAdmin) {
            return;
        }

        if (!currentUserId.equals(patientUserId)
                && !currentUserId.equals(doctorUserId)) {

            throw new ForbiddenException("Access denied");
        }
    }

}
