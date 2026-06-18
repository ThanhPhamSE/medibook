package com.medibook.modules.doctor.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpgradeToDoctorRequest {

    @NotNull
    private Long userId;

    @NotNull
    private Long specialtyId;

    @NotBlank
    private String degree;

    @NotNull
    @Min(0)
    private Integer experienceYears;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal consultationFee;

    @Size(max = 5000)
    private String biography;
}