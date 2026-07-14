package com.medibook.modules.appointment.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import com.medibook.common.enums.AppointmentStatus;
import com.medibook.common.response.ApiResponse;
import com.medibook.common.response.PageResponse;
import com.medibook.modules.appointment.dto.request.AppointmentCreateRequest;
import com.medibook.modules.appointment.dto.request.AppointmentRescheduleRequest;
import com.medibook.modules.appointment.dto.response.AppointmentResponse;
import com.medibook.modules.appointment.dto.response.AppointmentStatsResponse;
import com.medibook.modules.appointment.dto.response.BookedSlotResponse;
import com.medibook.modules.appointment.service.AppointmentService;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
@Validated
@PreAuthorize("isAuthenticated()")
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    public ResponseEntity<ApiResponse<AppointmentResponse>> createAppointment(
            @Valid @RequestBody AppointmentCreateRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "Appointment created successfully", appointmentService.createAppointment(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AppointmentResponse>> getAppointment(
            @PathVariable @Positive Long id) {

        return ResponseEntity.ok(ApiResponse.success(appointmentService.getAppointment(id)));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Page<AppointmentResponse>>> getMyAppointments(
            @RequestParam(required = false) AppointmentStatus status,
            @RequestParam(required = false, defaultValue = "all") String timeFilter,
            Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(appointmentService.getMyAppointments(status, timeFilter, pageable)));
    }

    @GetMapping("/me/stats")
    public ResponseEntity<ApiResponse<AppointmentStatsResponse>> getMyAppointmentsStats() {

        return ResponseEntity.ok(ApiResponse.success(appointmentService.getMyAppointmentsStats()));
    }


    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('CUSTOMER','DOCTOR','ADMIN')")
    public ResponseEntity<ApiResponse<Void>> cancelAppointment(@PathVariable @Positive Long id,
            @RequestParam(required = false) String reason) {

        appointmentService.cancelAppointment(id, reason);

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/slots/check")
    public ResponseEntity<ApiResponse<Boolean>> isSlotBooked(@RequestParam @Positive Long doctorId,
            @RequestParam @NotNull LocalDateTime startDatetime) {

        return ResponseEntity.ok(ApiResponse.success(appointmentService.isSlotBooked(doctorId, startDatetime)));
    }

    @GetMapping("/doctor/{doctorId}/booked")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    public ResponseEntity<ApiResponse<List<BookedSlotResponse>>> getBookedAppointmentsByDate(
            @PathVariable @Positive Long doctorId,
            @RequestParam @NotNull LocalDate date) {

        return ResponseEntity.ok(ApiResponse.success(appointmentService.getBookedAppointmentsByDate(doctorId, date)));
    }

    @PatchMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    public ResponseEntity<ApiResponse<Void>> confirm(@PathVariable @Positive Long id) {

        appointmentService.confirmAppointment(id);

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    public ResponseEntity<ApiResponse<Void>> complete(@PathVariable @Positive Long id) {

        appointmentService.completeAppointment(id);

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PatchMapping("/{id}/no-show")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    public ResponseEntity<ApiResponse<Void>> noShow(@PathVariable @Positive Long id) {

        appointmentService.markNoShow(id);

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/doctor/today")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    public ResponseEntity<ApiResponse<Page<AppointmentResponse>>> today(
            Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(appointmentService.getTodayAppointments(pageable)));
    }

    @GetMapping("/doctor/week")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    public ResponseEntity<ApiResponse<Page<AppointmentResponse>>> week(Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(appointmentService.getCurrentWeekAppointments(pageable)));
    }

    @PutMapping("/{id}/reschedule")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<AppointmentResponse>> rescheduleAppointment(@PathVariable @Positive Long id,
            @Valid @RequestBody AppointmentRescheduleRequest request) {

        return ResponseEntity.ok(ApiResponse.success(appointmentService.rescheduleAppointment(id, request)));
    }

    @GetMapping("/admin/bookings")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<AppointmentResponse>>> getAllBookings(Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(appointmentService.getAllBookings(pageable)));
    }

    @GetMapping("/admin/bookings/monthly")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<AppointmentResponse>>> monthlySchedule(
            @RequestParam(required = false) Long doctorId, @RequestParam(required = false) AppointmentStatus status,
            @RequestParam LocalDate from, @RequestParam LocalDate to, Pageable pageable) {

        return ResponseEntity
                .ok(ApiResponse.success(appointmentService.getMonthlySchedule(doctorId, status, from, to, pageable)));
    }

    @GetMapping("/admin/bookings/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<AppointmentResponse>>> searchBookings(
            @RequestParam(required = false) String bookingCode, @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) Long patientId, @RequestParam(required = false) AppointmentStatus status,
            @RequestParam(required = false) LocalDate from, @RequestParam(required = false) LocalDate to,
            Pageable pageable) {

        return ResponseEntity.ok(ApiResponse
                .success(appointmentService.searchAdminBookings(bookingCode, doctorId, patientId, status, from, to,
                        pageable)));
    }
}