package com.medibook.modules.medicalrecord.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MedicalRecordCreateRequest {

    @NotNull
    private Long appointmentId;

    @NotBlank
    private String diagnosis;

    private String prescription;

    private String note;
}
