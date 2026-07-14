package com.medibook.modules.appointment.dto.internal;

import com.medibook.common.enums.AppointmentStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentReviewInfoResponse {

    private Long appointmentId;

    private String bookingCode;

    private Long doctorId;

    private Long patientId;

    private String patientName;

    private AppointmentStatus status;

}
