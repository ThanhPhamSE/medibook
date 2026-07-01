package com.medibook.modules.review.dto.response;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ReviewResponse {

    private Long id;

    private Long appointmentId;

    private String bookingCode;

    private Long doctorId;

    private Long patientId;

    private String patientName;

    private Integer rating;

    private String comment;

    private LocalDateTime createdAt;

}
