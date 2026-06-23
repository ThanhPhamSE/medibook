package com.medibook.modules.review.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.medibook.modules.review.dto.request.ReviewCreateRequest;
import com.medibook.modules.review.dto.response.DoctorRatingResponse;
import com.medibook.modules.review.dto.response.ReviewResponse;
import com.medibook.modules.review.service.ReviewService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ReviewResponse createReview(@Valid @RequestBody ReviewCreateRequest request) {

        return reviewService.createReview(request);
    }

    @GetMapping("/doctor/{doctorId}")
    public Page<ReviewResponse> getDoctorReviews(@PathVariable Long doctorId, Pageable pageable) {

        return reviewService.getDoctorReviews(doctorId, pageable);
    }

    @GetMapping("/doctor/{doctorId}/rating")
    public DoctorRatingResponse getDoctorRating(@PathVariable Long doctorId) {
        return reviewService.getDoctorRating(doctorId);
    }
}
