package com.medibook.modules.review.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewCreateRequest {

    @NotNull(message = "Appointment id is required")
    private Long appointmentId;

    @NotNull
    @Min(value = 1)
    @Max(value = 5)
    private Integer rating;

    private String comment;

}
