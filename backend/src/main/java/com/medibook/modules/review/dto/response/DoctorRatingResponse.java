package com.medibook.modules.review.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DoctorRatingResponse {

    private Double averageRating;

    private Long totalReviews;

}
