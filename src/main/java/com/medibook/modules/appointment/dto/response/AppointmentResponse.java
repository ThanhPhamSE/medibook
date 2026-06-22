package com.medibook.modules.appointment.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.medibook.common.enums.AppointmentStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResponse {

    private Long id;

    private String bookingCode;

    private Long doctorId;

    private String doctorName;

    private Long patientId;

    private String patientName;

    private BigDecimal consultationFee;

    private LocalDateTime startDatetime;

    private LocalDateTime endDatetime;

    private AppointmentStatus status;

    private String note;

}
