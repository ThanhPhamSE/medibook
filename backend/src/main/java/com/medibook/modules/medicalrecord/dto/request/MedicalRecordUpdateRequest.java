package com.medibook.modules.medicalrecord.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MedicalRecordUpdateRequest {
    @NotBlank
    private String diagnosis;

    private String prescription;

    private String note;
}
