package com.medibook.modules.review.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.medibook.common.response.ApiResponse;
import com.medibook.modules.review.dto.request.ReviewCreateRequest;
import com.medibook.modules.review.dto.response.DoctorRatingResponse;
import com.medibook.modules.review.dto.response.ReviewResponse;
import com.medibook.modules.review.service.ReviewService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(@Valid @RequestBody ReviewCreateRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(reviewService.createReview(request)));
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getDoctorReviews(@PathVariable Long doctorId,
            Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(reviewService.getDoctorReviews(doctorId, pageable)));
    }

    @GetMapping("/doctor/{doctorId}/rating")
    public ResponseEntity<ApiResponse<DoctorRatingResponse>> getDoctorRating(@PathVariable Long doctorId) {
        return ResponseEntity.ok(ApiResponse.success(reviewService.getDoctorRating(doctorId)));
    }
}
