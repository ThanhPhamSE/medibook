package com.medibook.modules.medicalrecord.dto.response;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MedicalRecordResponse {

    private Long id;

    private Long appointmentId;

    private String bookingCode;

    private Long patientId;

    private String patientName;

    private Long doctorId;

    private String doctorName;

    private String diagnosis;

    private String prescription;

    private String note;

    private LocalDateTime createdAt;

}
