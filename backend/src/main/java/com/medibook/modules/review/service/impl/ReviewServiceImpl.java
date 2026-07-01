package com.medibook.modules.review.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.medibook.common.enums.AppointmentStatus;
import com.medibook.common.exception.BadRequestException;
import com.medibook.common.exception.ForbiddenException;
import com.medibook.modules.appointment.dto.internal.AppointmentReviewInfoResponse;
import com.medibook.modules.appointment.facade.AppointmentFacade;
import com.medibook.modules.review.dto.request.ReviewCreateRequest;
import com.medibook.modules.review.dto.response.DoctorRatingResponse;
import com.medibook.modules.review.dto.response.ReviewResponse;
import com.medibook.modules.review.entity.Review;
import com.medibook.modules.review.mapper.ReviewMapper;
import com.medibook.modules.review.repository.ReviewRepository;
import com.medibook.modules.review.service.ReviewService;
import com.medibook.modules.user.dto.internal.CurrentUserResponse;
import com.medibook.modules.user.facade.UserFacade;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;
    private final AppointmentFacade appointmentFacade;
    private final UserFacade userFacade;

    @Override
    public ReviewResponse createReview(ReviewCreateRequest request) {
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
        Review review = new Review();

        review.setAppointment(appointmentFacade.getAppointmentEntity(appointment.getAppointmentId()));
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        reviewRepository.save(review);

        return reviewMapper.toResponse(review);
    }

    @Override
    public Page<ReviewResponse> getDoctorReviews(Long doctorId, Pageable pageable) {
        return reviewRepository.findByAppointmentDoctorId(doctorId, pageable).map(reviewMapper::toResponse);
    }

    @Override
    public DoctorRatingResponse getDoctorRating(
            Long doctorId) {
        Double average = reviewRepository.getAverageRating(doctorId);

        Long total = reviewRepository.countByAppointmentDoctorId(doctorId);

        return new DoctorRatingResponse(average == null ? 0.0 : average, total);
    }

}
