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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Appointments", description = "Appointment management APIs")
@SecurityRequirement(name = "Bearer")
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    @Operation(summary = "Create a new appointment", description = "Book an appointment with a doctor for a specific time slot")
    public ResponseEntity<ApiResponse<AppointmentResponse>> createAppointment(
            @Valid @RequestBody AppointmentCreateRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "Appointment created successfully",
                        appointmentService.createAppointment(request)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get appointment by ID", description = "Retrieve appointment details by appointment ID")
    public ResponseEntity<ApiResponse<AppointmentResponse>> getAppointment(
            @Parameter(description = "Appointment ID") @PathVariable @Positive Long id) {

        return ResponseEntity.ok(ApiResponse.success(appointmentService.getAppointment(id)));
    }

    @GetMapping("/me")
    @Operation(summary = "Get my appointments", description = "Retrieve current user's appointments with optional filters")
    public ResponseEntity<ApiResponse<Page<AppointmentResponse>>> getMyAppointments(
            @Parameter(description = "Filter by appointment status") @RequestParam(required = false) AppointmentStatus status,
            @Parameter(description = "Time filter: upcoming, past, or all") @RequestParam(required = false, defaultValue = "all") String timeFilter,
            Pageable pageable) {

        return ResponseEntity
                .ok(ApiResponse.success(appointmentService.getMyAppointments(status, timeFilter, pageable)));
    }

    @GetMapping("/me/stats")
    @Operation(summary = "Get my appointment statistics", description = "Retrieve current user's appointment statistics")
    public ResponseEntity<ApiResponse<AppointmentStatsResponse>> getMyAppointmentsStats() {

        return ResponseEntity.ok(ApiResponse.success(appointmentService.getMyAppointmentsStats()));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('CUSTOMER','DOCTOR','ADMIN')")
    @Operation(summary = "Cancel appointment", description = "Cancel an appointment with a reason (at least 24 hours in advance)")
    public ResponseEntity<ApiResponse<Void>> cancelAppointment(
            @Parameter(description = "Appointment ID") @PathVariable @Positive Long id,
            @Parameter(description = "Cancellation reason") @RequestParam(required = false) String reason) {

        appointmentService.cancelAppointment(id, reason);

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/slots/check")
    @Operation(summary = "Check if slot is booked", description = "Check if a specific time slot for a doctor is already booked")
    public ResponseEntity<ApiResponse<Boolean>> isSlotBooked(
            @Parameter(description = "Doctor ID") @RequestParam @Positive Long doctorId,
            @Parameter(description = "Start datetime of the slot") @RequestParam @NotNull LocalDateTime startDatetime) {

        return ResponseEntity.ok(ApiResponse.success(appointmentService.isSlotBooked(doctorId, startDatetime)));
    }

    @GetMapping("/doctor/{doctorId}/booked")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    @Operation(summary = "Get booked slots by date", description = "Retrieve all booked appointment slots for a doctor on a specific date")
    public ResponseEntity<ApiResponse<List<BookedSlotResponse>>> getBookedAppointmentsByDate(
            @Parameter(description = "Doctor ID") @PathVariable @Positive Long doctorId,
            @Parameter(description = "Date to check") @RequestParam @NotNull LocalDate date) {

        return ResponseEntity.ok(ApiResponse.success(appointmentService.getBookedAppointmentsByDate(doctorId, date)));
    }

    @PatchMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    @Operation(summary = "Confirm appointment", description = "Confirm a pending appointment (Admin/Doctor only)")
    public ResponseEntity<ApiResponse<Void>> confirm(
            @Parameter(description = "Appointment ID") @PathVariable @Positive Long id) {

        appointmentService.confirmAppointment(id);

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    @Operation(summary = "Complete appointment", description = "Mark an appointment as completed (Admin/Doctor only)")
    public ResponseEntity<ApiResponse<Void>> complete(
            @Parameter(description = "Appointment ID") @PathVariable @Positive Long id) {

        appointmentService.completeAppointment(id);

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PatchMapping("/{id}/no-show")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    @Operation(summary = "Mark as no-show", description = "Mark an appointment as no-show (Admin/Doctor only)")
    public ResponseEntity<ApiResponse<Void>> noShow(
            @Parameter(description = "Appointment ID") @PathVariable @Positive Long id) {

        appointmentService.markNoShow(id);

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/doctor/today")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    @Operation(summary = "Get today's appointments", description = "Retrieve doctor's appointments for today (Admin/Doctor only)")
    public ResponseEntity<ApiResponse<Page<AppointmentResponse>>> today(
            Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(appointmentService.getTodayAppointments(pageable)));
    }

    @GetMapping("/doctor/week")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    @Operation(summary = "Get current week appointments", description = "Retrieve doctor's appointments for the current week (Admin/Doctor only)")
    public ResponseEntity<ApiResponse<Page<AppointmentResponse>>> week(Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(appointmentService.getCurrentWeekAppointments(pageable)));
    }

    @PutMapping("/{id}/reschedule")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Reschedule appointment", description = "Reschedule an appointment to a new time slot (Customer only)")
    public ResponseEntity<ApiResponse<AppointmentResponse>> rescheduleAppointment(
            @Parameter(description = "Appointment ID") @PathVariable @Positive Long id,
            @Valid @RequestBody AppointmentRescheduleRequest request) {

        return ResponseEntity.ok(ApiResponse.success(appointmentService.rescheduleAppointment(id, request)));
    }

    @GetMapping("/admin/bookings")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all bookings (Admin)", description = "Retrieve all appointments in the system (Admin only)")
    public ResponseEntity<ApiResponse<PageResponse<AppointmentResponse>>> getAllBookings(Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(appointmentService.getAllBookings(pageable)));
    }

    @GetMapping("/admin/bookings/monthly")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get monthly schedule (Admin)", description = "Retrieve monthly appointment schedule with filters (Admin only)")
    public ResponseEntity<ApiResponse<PageResponse<AppointmentResponse>>> monthlySchedule(
            @Parameter(description = "Filter by doctor ID") @RequestParam(required = false) Long doctorId,
            @Parameter(description = "Filter by appointment status") @RequestParam(required = false) AppointmentStatus status,
            @Parameter(description = "Start date") @RequestParam LocalDate from,
            @Parameter(description = "End date") @RequestParam LocalDate to,
            Pageable pageable) {

        return ResponseEntity
                .ok(ApiResponse.success(appointmentService.getMonthlySchedule(doctorId, status, from, to, pageable)));
    }

    @GetMapping("/admin/bookings/search")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Search bookings (Admin)", description = "Search appointments with multiple filters (Admin only)")
    public ResponseEntity<ApiResponse<PageResponse<AppointmentResponse>>> searchBookings(
            @Parameter(description = "Filter by booking code") @RequestParam(required = false) String bookingCode,
            @Parameter(description = "Filter by doctor ID") @RequestParam(required = false) Long doctorId,
            @Parameter(description = "Filter by patient ID") @RequestParam(required = false) Long patientId,
            @Parameter(description = "Filter by appointment status") @RequestParam(required = false) AppointmentStatus status,
            @Parameter(description = "Start date") @RequestParam(required = false) LocalDate from,
            @Parameter(description = "End date") @RequestParam(required = false) LocalDate to,
            Pageable pageable) {

        return ResponseEntity.ok(ApiResponse
                .success(appointmentService.searchAdminBookings(bookingCode, doctorId, patientId, status, from, to,
                        pageable)));
    }
}