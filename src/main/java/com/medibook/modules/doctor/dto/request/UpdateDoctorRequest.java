package com.medibook.modules.doctor.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateDoctorRequest {

    @NotNull(message = "Specialty ID is required")
    private Long specialtyId;

    @Size(max = 255, message = "Degree cannot exceed 255 characters")
    private String degree;

    @Min(value = 0, message = "Experience years cannot be negative")
    private Integer experienceYears;

    @DecimalMin(value = "0.0", inclusive = false, message = "Consultation fee must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Invalid consultation fee format")
    private BigDecimal consultationFee;

    @Size(max = 5000, message = "Biography cannot exceed 5000 characters")
    private String biography;
}
