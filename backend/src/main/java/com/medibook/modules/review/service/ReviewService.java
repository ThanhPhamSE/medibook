package com.medibook.modules.review.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.medibook.modules.review.dto.request.ReviewCreateRequest;
import com.medibook.modules.review.dto.response.DoctorRatingResponse;
import com.medibook.modules.review.dto.response.ReviewResponse;

public interface ReviewService {

    ReviewResponse createReview(ReviewCreateRequest request);

    Page<ReviewResponse> getDoctorReviews(Long doctorId, Pageable pageable);

    DoctorRatingResponse getDoctorRating(Long doctorId);

}
