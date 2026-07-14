package com.medibook.modules.schedule.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.medibook.common.response.ApiResponse;
import com.medibook.modules.schedule.dto.request.SlotGenerateRequest;
import com.medibook.modules.schedule.dto.request.TimeOffRequest;
import com.medibook.modules.schedule.dto.request.TimeOffUpdateRequest;
import com.medibook.modules.schedule.dto.request.WorkingPatternRequest;
import com.medibook.modules.schedule.dto.request.WorkingPatternUpdateRequest;
import com.medibook.modules.schedule.dto.response.DoctorScheduleResponse;
import com.medibook.modules.schedule.dto.response.SlotResponse;
import com.medibook.modules.schedule.dto.response.TimeOffResponse;
import com.medibook.modules.schedule.dto.response.WorkingPatternResponse;
import com.medibook.modules.schedule.service.ScheduleService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/schedules")
@RequiredArgsConstructor
@Validated
public class ScheduleController {

    private final ScheduleService scheduleService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/working-patterns")
    public ResponseEntity<ApiResponse<WorkingPatternResponse>> createWorkingPattern(
            @Valid @RequestBody WorkingPatternRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "Working pattern created successfully", scheduleService.createWorkingPattern(request)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/working-patterns/{id}")
    public ResponseEntity<ApiResponse<WorkingPatternResponse>> updateWorkingPattern(@PathVariable @Positive Long id,
            @Valid @RequestBody WorkingPatternUpdateRequest request) {

        return ResponseEntity.ok(ApiResponse.success(scheduleService.updateWorkingPattern(id, request)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/working-patterns/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteWorkingPattern(@PathVariable @Positive Long id) {

        scheduleService.deleteWorkingPattern(id);

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/time-offs")
    public ResponseEntity<ApiResponse<TimeOffResponse>> createTimeOff(@Valid @RequestBody TimeOffRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "Time off created successfully", scheduleService.createTimeOff(request)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/time-offs/{id}")
    public ResponseEntity<ApiResponse<TimeOffResponse>> updateTimeOff(@PathVariable @Positive Long id,
            @Valid @RequestBody TimeOffUpdateRequest request) {

        return ResponseEntity.ok(ApiResponse.success(scheduleService.updateTimeOff(id, request)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/time-offs/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTimeOff(@PathVariable @Positive Long id) {

        scheduleService.deleteTimeOff(id);

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/slots")
    public ResponseEntity<ApiResponse<List<SlotResponse>>> generateSlots(
            @Valid @RequestBody SlotGenerateRequest request) {

        return ResponseEntity.ok(ApiResponse.success(scheduleService.generateSlots(request)));
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<ApiResponse<DoctorScheduleResponse>> getDoctorSchedule(
            @PathVariable @Positive Long doctorId) {

        return ResponseEntity.ok(ApiResponse.success(scheduleService.getDoctorSchedule(doctorId)));
    }
}