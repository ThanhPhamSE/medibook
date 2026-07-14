package com.medibook.modules.review.service;

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
import com.medibook.modules.review.service.impl.ReviewServiceImpl;
import com.medibook.modules.user.dto.internal.CurrentUserResponse;
import com.medibook.modules.user.facade.UserFacade;
import com.medibook.modules.audit.service.AuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ReviewMapper reviewMapper;

    @Mock
    private AppointmentFacade appointmentFacade;

    @Mock
    private UserFacade userFacade;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private ReviewCreateRequest createRequest;
    private Review review;
    private AppointmentReviewInfoResponse appointmentInfo;
    private CurrentUserResponse currentUser;

    @BeforeEach
    void setUp() {
        createRequest = new ReviewCreateRequest();
        createRequest.setAppointmentId(1L);
        createRequest.setRating(5);
        createRequest.setComment("Excellent service");

        review = new Review();
        review.setId(1L);
        review.setRating(5);
        review.setComment("Excellent service");

        appointmentInfo = new AppointmentReviewInfoResponse();
        appointmentInfo.setAppointmentId(1L);
        appointmentInfo.setDoctorId(1L);
        appointmentInfo.setPatientId(2L);
        appointmentInfo.setStatus(AppointmentStatus.COMPLETED);

        currentUser = new CurrentUserResponse(2L, "John Doe");
    }

    @Test
    void createReview_Success() {
        when(userFacade.getCurrentUserAndName()).thenReturn(currentUser);
        when(appointmentFacade.getReviewInfo(1L)).thenReturn(appointmentInfo);
        when(reviewRepository.existsByAppointmentId(1L)).thenReturn(false);
        when(reviewMapper.toEntity(any(ReviewCreateRequest.class))).thenReturn(review);
        when(appointmentFacade.getAppointmentEntity(1L))
                .thenReturn(mock(com.medibook.modules.appointment.entity.Appointment.class));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);
        when(reviewRepository.getAverageRating(1L)).thenReturn(4.5);
        when(reviewRepository.countByAppointmentDoctorId(1L)).thenReturn(10L);
        doNothing().when(doctorRepository).updateRating(anyLong(), any(), anyInt());
        when(reviewMapper.toResponse(any(Review.class))).thenReturn(new ReviewResponse());
        doNothing().when(auditService).log(anyString(), anyString(), anyLong(), any(), any());

        ReviewResponse response = reviewService.createReview(createRequest);

        assertThat(response).isNotNull();
        verify(reviewRepository).save(any(Review.class));
        verify(doctorRepository).updateRating(1L, any(), anyInt());
        verify(auditService).log(eq("CREATE"), eq("Review"), anyLong(), any(), any());
    }

    @Test
    void createReview_AppointmentIdNull() {
        createRequest.setAppointmentId(null);

        assertThatThrownBy(() -> reviewService.createReview(createRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Appointment id is required");

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void createReview_RatingOutOfRange() {
        createRequest.setRating(6);

        assertThatThrownBy(() -> reviewService.createReview(createRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Rating must be between 1 and 5");

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void createReview_RatingBelowRange() {
        createRequest.setRating(0);

        assertThatThrownBy(() -> reviewService.createReview(createRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Rating must be between 1 and 5");

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void createReview_NotPatientsAppointment() {
        CurrentUserResponse otherUser = new CurrentUserResponse(3L, "Other User");
        when(userFacade.getCurrentUserAndName()).thenReturn(otherUser);
        when(appointmentFacade.getReviewInfo(1L)).thenReturn(appointmentInfo);

        assertThatThrownBy(() -> reviewService.createReview(createRequest))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("can only review your own appointment");

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void createReview_AppointmentNotCompleted() {
        appointmentInfo.setStatus(AppointmentStatus.PENDING);
        when(userFacade.getCurrentUserAndName()).thenReturn(currentUser);
        when(appointmentFacade.getReviewInfo(1L)).thenReturn(appointmentInfo);

        assertThatThrownBy(() -> reviewService.createReview(createRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Appointment is not completed");

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void createReview_AlreadyReviewed() {
        when(userFacade.getCurrentUserAndName()).thenReturn(currentUser);
        when(appointmentFacade.getReviewInfo(1L)).thenReturn(appointmentInfo);
        when(reviewRepository.existsByAppointmentId(1L)).thenReturn(true);

        assertThatThrownBy(() -> reviewService.createReview(createRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Appointment already reviewed");

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void getDoctorReviews_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Review> reviewPage = new PageImpl<>(List.of(review));

        when(reviewRepository.findByAppointmentDoctorId(1L, pageable)).thenReturn(reviewPage);
        when(reviewMapper.toResponse(any(Review.class))).thenReturn(new ReviewResponse());

        Page<ReviewResponse> response = reviewService.getDoctorReviews(1L, pageable);

        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        verify(reviewRepository).findByAppointmentDoctorId(1L, pageable);
    }

    @Test
    void getDoctorRating_Success() {
        when(reviewRepository.getAverageRating(1L)).thenReturn(4.5);
        when(reviewRepository.countByAppointmentDoctorId(1L)).thenReturn(10L);

        DoctorRatingResponse response = reviewService.getDoctorRating(1L);

        assertThat(response).isNotNull();
        assertThat(response.getAverageRating()).isEqualTo(4.5);
        assertThat(response.getTotalReviews()).isEqualTo(10);
        verify(reviewRepository).getAverageRating(1L);
        verify(reviewRepository).countByAppointmentDoctorId(1L);
    }

    @Test
    void getDoctorRating_NoReviews() {
        when(reviewRepository.getAverageRating(1L)).thenReturn(null);
        when(reviewRepository.countByAppointmentDoctorId(1L)).thenReturn(0L);

        DoctorRatingResponse response = reviewService.getDoctorRating(1L);

        assertThat(response).isNotNull();
        assertThat(response.getAverageRating()).isEqualTo(0.0);
        assertThat(response.getTotalReviews()).isEqualTo(0);
    }
}
