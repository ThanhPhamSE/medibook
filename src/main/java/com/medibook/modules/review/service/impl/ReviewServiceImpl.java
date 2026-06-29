package com.medibook.modules.review.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.medibook.common.enums.AppointmentStatus;
import com.medibook.common.exception.BadRequestException;
import com.medibook.common.exception.ForbiddenException;
import com.medibook.modules.appointment.entity.Appointment;
import com.medibook.modules.appointment.service.AppointmentService;
import com.medibook.modules.review.dto.request.ReviewCreateRequest;
import com.medibook.modules.review.dto.response.DoctorRatingResponse;
import com.medibook.modules.review.dto.response.ReviewResponse;
import com.medibook.modules.review.entity.Review;
import com.medibook.modules.review.mapper.ReviewMapper;
import com.medibook.modules.review.repository.ReviewRepository;
import com.medibook.modules.review.service.ReviewService;
import com.medibook.modules.user.entity.User;
import com.medibook.modules.user.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final AppointmentService appointmentService;
    private final ReviewMapper reviewMapper;
    private final UserService userService;

    @Override
    public ReviewResponse createReview(ReviewCreateRequest request) {
        Appointment appointment = appointmentService.getAppointmentEntity(request.getAppointmentId());

        User currentUser = userService.getCurrentUser();

        if (!appointment.getPatient().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You can only review your own appointment");
        }

        if (appointment.getStatus() != AppointmentStatus.COMPLETED) {
            throw new BadRequestException("Appointment is not completed");
        }
        if (reviewRepository.existsByAppointmentId(appointment.getId())) {
            throw new BadRequestException("Appointment already reviewed");
        }
        Review review = new Review();

        review.setAppointment(appointment);
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
