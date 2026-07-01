package com.medibook.modules.doctor.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DoctorSearchRequest {

    @Size(max = 100, message = "Keyword cannot exceed 100 characters")
    private String keyword;

    @Min(value = 1, message = "Specialty ID must be greater than 0")
    private Long specialtyId;

    @Min(value = 0, message = "Minimum experience cannot be negative")
    private Integer minExperience;

    @Min(value = 0, message = "Maximum experience cannot be negative")
    private Integer maxExperience;

    @DecimalMin(value = "0.0", inclusive = true, message = "Minimum fee cannot be negative")
    private BigDecimal minFee;

    @DecimalMin(value = "0.0", inclusive = true, message = "Maximum fee cannot be negative")
    private BigDecimal maxFee;

    @DecimalMin(value = "0.0", inclusive = true, message = "Minimum rating cannot be negative")
    private BigDecimal minRating;

}
