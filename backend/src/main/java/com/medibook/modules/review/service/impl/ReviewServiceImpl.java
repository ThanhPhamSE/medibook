package com.medibook.modules.review.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.medibook.common.enums.AppointmentStatus;
import com.medibook.common.exception.BadRequestException;
import com.medibook.common.exception.ForbiddenException;
import com.medibook.modules.appointment.dto.internal.AppointmentReviewInfoResponse;
import com.medibook.modules.appointment.facade.AppointmentFacade;
import com.medibook.modules.doctor.repository.DoctorRepository;
import com.medibook.modules.review.dto.request.ReviewCreateRequest;
import com.medibook.modules.review.dto.response.DoctorRatingResponse;
import com.medibook.modules.review.dto.response.ReviewResponse;
import com.medibook.modules.review.entity.Review;
import com.medibook.modules.review.mapper.ReviewMapper;
import com.medibook.modules.review.repository.ReviewRepository;
import com.medibook.modules.review.service.ReviewService;
import com.medibook.modules.user.dto.internal.CurrentUserResponse;
import com.medibook.modules.user.facade.UserFacade;
import com.medibook.modules.audit.service.AuditService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;
    private final AppointmentFacade appointmentFacade;
    private final UserFacade userFacade;
    private final DoctorRepository doctorRepository;
    private final AuditService auditService;

    @Override
    @Transactional
    public ReviewResponse createReview(ReviewCreateRequest request) {
        log.info("Request to create review: appointmentId={}, rating={}", request.getAppointmentId(), request.getRating());

        if (request.getAppointmentId() == null) {
            throw new BadRequestException("Appointment id is required");
        }

        if (request.getRating() == null || request.getRating() < 1 || request.getRating() > 5) {
            throw new BadRequestException("Rating must be between 1 and 5");
        }

        AppointmentReviewInfoResponse appointment = appointmentFacade.getReviewInfo(
                request.getAppointmentId());

        CurrentUserResponse currentUser = userFacade.getCurrentUserAndName();

        if (!appointment.getPatientId().equals(currentUser.getId())) {
            throw new ForbiddenException("You can only review your own appointment");
        }

        if (appointment.getStatus() != AppointmentStatus.COMPLETED) {
            throw new BadRequestException("Appointment is not completed");
        }
        if (reviewRepository.existsByAppointmentId(appointment.getAppointmentId())) {
            throw new BadRequestException("Appointment already reviewed");
        }

        Review review = reviewMapper.toEntity(request);
        review.setAppointment(appointmentFacade.getAppointmentEntity(appointment.getAppointmentId()));

        reviewRepository.save(review);

        // Update doctor rating
        Long doctorId = appointment.getDoctorId();
        Double average = reviewRepository.getAverageRating(doctorId);
        Long total = reviewRepository.countByAppointmentDoctorId(doctorId);

        BigDecimal averageRating = average != null 
            ? BigDecimal.valueOf(average).setScale(2, RoundingMode.HALF_UP) 
            : BigDecimal.ZERO;
        
        doctorRepository.updateRating(doctorId, averageRating, total.intValue());

        auditService.log("CREATE", "Review", review.getId(), null, review);

        log.info("Successfully created review: reviewId={}, appointmentId={}, rating={}", review.getId(), request.getAppointmentId(), request.getRating());

        return reviewMapper.toResponse(review);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getDoctorReviews(Long doctorId, Pageable pageable) {
        return reviewRepository.findByAppointmentDoctorId(doctorId, pageable).map(reviewMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public DoctorRatingResponse getDoctorRating(
            Long doctorId) {
        Double average = reviewRepository.getAverageRating(doctorId);

        Long total = reviewRepository.countByAppointmentDoctorId(doctorId);

        return new DoctorRatingResponse(average == null ? 0.0 : average, total);
    }

}
