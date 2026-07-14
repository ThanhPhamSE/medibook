package com.medibook.modules.doctor.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorResponse {

    private Long id;

    private Long userId;

    private String fullName;

    private String email;

    private String phone;

    private String profileImage;

    private Long specialtyId;

    private String specialtyName;

    private String degree;

    private Integer experienceYears;

    private BigDecimal consultationFee;

    private String biography;

    private BigDecimal averageRating;

    private Integer totalReviews;

    private Boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
