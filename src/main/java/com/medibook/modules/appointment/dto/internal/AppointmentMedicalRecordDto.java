package com.medibook.modules.appointment.dto.internal;

import com.medibook.common.enums.AppointmentStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AppointmentMedicalRecordDto {

    private Long appointmentId;

    private AppointmentStatus status;

    private Long doctorUserId;

    private Long patientUserId;

    private String bookingCode;

}
