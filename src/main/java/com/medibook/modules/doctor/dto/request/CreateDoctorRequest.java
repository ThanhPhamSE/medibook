package com.medibook.modules.doctor.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateDoctorRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Specialty ID is required")
    private Long specialtyId;

    @NotBlank(message = "Degree is required")
    @Size(max = 255, message = "Degree cannot exceed 255 characters")
    private String degree;

    @NotNull(message = "Experience years is required")
    @Min(value = 0, message = "Experience years cannot be negative")
    private Integer experienceYears;

    @NotNull(message = "Consultation fee is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Consultation fee must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Invalid consultation fee format")
    private BigDecimal consultationFee;

    @Size(max = 5000, message = "Biography cannot exceed 5000 characters")
    private String biography;
}
