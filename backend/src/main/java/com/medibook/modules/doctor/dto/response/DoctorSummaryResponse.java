package com.medibook.modules.doctor.dto.response;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DoctorSummaryResponse {

    private Long id;

    private String fullName;

    private String specialtyName;

    private Integer experienceYears;

    private BigDecimal consultationFee;

    private BigDecimal averageRating;

    private Integer totalReviews;

    private String profileImage;

}
