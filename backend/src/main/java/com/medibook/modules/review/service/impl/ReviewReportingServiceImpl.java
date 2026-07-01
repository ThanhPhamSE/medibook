package com.medibook.modules.review.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.medibook.modules.review.repository.ReviewRepository;
import com.medibook.modules.review.service.ReviewReportingService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewReportingServiceImpl implements ReviewReportingService {

    private final ReviewRepository reviewRepository;

    @Override
    public Double getAverageRating(Long doctorId) {

        return reviewRepository.getAverageRating(doctorId);

    }

}
