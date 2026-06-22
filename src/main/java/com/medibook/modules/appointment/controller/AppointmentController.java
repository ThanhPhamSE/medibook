package com.medibook.modules.appointment.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.medibook.common.response.ApiResponse;
import com.medibook.modules.appointment.dto.request.AppointmentCreateRequest;
import com.medibook.modules.appointment.dto.response.AppointmentResponse;
import com.medibook.modules.appointment.service.AppointmentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@Validated
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    public ResponseEntity<ApiResponse<AppointmentResponse>> createAppointment(
            @Valid @RequestBody AppointmentCreateRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(appointmentService.createAppointment(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AppointmentResponse>> getAppointment(
            @PathVariable Long id) {

        return ResponseEntity.ok(ApiResponse.success(appointmentService.getAppointment(id)));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Page<AppointmentResponse>>> getMyAppointments(
            Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(appointmentService.getMyAppointments(pageable)));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelAppointment(@PathVariable Long id,
            @RequestParam(required = false) String reason) {

        appointmentService.cancelAppointment(id, reason);

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/slots/check")
    public ResponseEntity<ApiResponse<Boolean>> isSlotBooked(@RequestParam Long doctorId,
            @RequestParam LocalDateTime startDatetime) {

        return ResponseEntity.ok(ApiResponse.success(appointmentService.isSlotBooked(doctorId, startDatetime)));
    }

    @GetMapping("/doctor/{doctorId}/booked")
    public ResponseEntity<ApiResponse<?>> getBookedAppointmentsByDate(@PathVariable Long doctorId,
            @RequestParam LocalDate date) {

        return ResponseEntity.ok(ApiResponse.success(appointmentService.getBookedAppointmentsByDate(doctorId, date)));
    }
}