package com.medibook.modules.review.facade.impl;

import org.springframework.stereotype.Service;

import com.medibook.modules.review.facade.ReviewReportingFacade;
import com.medibook.modules.review.service.ReviewReportingService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewReportingFacadeImpl implements ReviewReportingFacade {

    private ReviewReportingService reportingService;

    @Override
    public Double getAverageRating(Long doctorId) {

        return reportingService.getAverageRating(doctorId);

    }

}
