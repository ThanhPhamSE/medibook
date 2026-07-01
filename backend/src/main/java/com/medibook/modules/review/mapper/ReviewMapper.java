package com.medibook.modules.review.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.medibook.modules.review.dto.request.ReviewCreateRequest;
import com.medibook.modules.review.dto.response.ReviewResponse;
import com.medibook.modules.review.entity.Review;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "appointment", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    Review toEntity(ReviewCreateRequest request);

    @Mapping(target = "appointmentId", source = "appointment.id")
    @Mapping(target = "bookingCode", source = "appointment.bookingCode")
    @Mapping(target = "doctorId", source = "appointment.doctor.id")
    @Mapping(target = "patientId", source = "appointment.patient.id")
    @Mapping(target = "patientName", source = "appointment.patient.fullName")
    ReviewResponse toResponse(Review review);
}