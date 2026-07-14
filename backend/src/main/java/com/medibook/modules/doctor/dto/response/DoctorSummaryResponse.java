package com.medibook.modules.doctor.dto.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorSummaryResponse {

    private Long id;

    private Long specialtyId;

    private String fullName;

    private String specialtyName;

    private Integer experienceYears;

    private BigDecimal consultationFee;

    private BigDecimal averageRating;

    private Integer totalReviews;

    private String profileImage;

    private String degree;

    private String biography;

}
