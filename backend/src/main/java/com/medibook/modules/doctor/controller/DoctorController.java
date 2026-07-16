package com.medibook.modules.doctor.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.medibook.common.response.ApiResponse;
import com.medibook.common.response.PageResponse;
import com.medibook.modules.doctor.dto.request.CreateDoctorRequest;
import com.medibook.modules.doctor.dto.request.DoctorSearchRequest;
import com.medibook.modules.doctor.dto.request.UpdateDoctorRequest;
import com.medibook.modules.doctor.dto.request.UpgradeToDoctorRequest;
import com.medibook.modules.doctor.dto.response.DoctorResponse;
import com.medibook.modules.doctor.dto.response.DoctorSummaryResponse;
import com.medibook.modules.doctor.service.DoctorService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/doctors")
@RequiredArgsConstructor
@Validated
@Tag(name = "Doctors", description = "Doctor management APIs")
public class DoctorController {

    private final DoctorService doctorService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create doctor profile", description = "Create a new doctor profile (Admin only)")
    public ResponseEntity<ApiResponse<DoctorResponse>> createDoctor(@Valid @RequestBody CreateDoctorRequest request) {

        DoctorResponse response = doctorService.createDoctor(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "Doctor profile created successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get doctor by ID", description = "Retrieve doctor profile by ID")
    public ResponseEntity<ApiResponse<DoctorResponse>> getDoctorById(
            @Parameter(description = "Doctor ID") @PathVariable Long id) {

        return ResponseEntity.ok(ApiResponse.success(doctorService.getDoctorById(id)));
    }

    @GetMapping
    @Operation(summary = "Search doctors", description = "Search doctors with filters and pagination")
    public ResponseEntity<ApiResponse<PageResponse<DoctorSummaryResponse>>> searchDoctors(
            @ModelAttribute DoctorSearchRequest request, @PageableDefault(size = 10) Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(doctorService.searchDoctors(request, pageable)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    @Operation(summary = "Update doctor profile", description = "Update doctor profile information (Admin/Doctor only)")
    public ResponseEntity<ApiResponse<DoctorResponse>> updateDoctor(
            @Parameter(description = "Doctor ID") @PathVariable Long id,
            @Valid @RequestBody UpdateDoctorRequest request) {

        return ResponseEntity.ok(ApiResponse.success(doctorService.updateDoctor(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete doctor", description = "Soft delete doctor profile (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteDoctor(
            @Parameter(description = "Doctor ID") @PathVariable Long id) {

        doctorService.deleteDoctor(id);

        return ResponseEntity.ok(ApiResponse.success("Deleted successful", null));
    }

    @PostMapping("/upgrade-to-doctor")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Upgrade user to doctor", description = "Upgrade existing user to doctor role (Admin only)")
    public ResponseEntity<ApiResponse<DoctorResponse>> upgradeToDoctor(
            @Valid @RequestBody UpgradeToDoctorRequest request) {

        return ResponseEntity.ok(ApiResponse.success("Updated successful", doctorService.upgradeToDoctor(request)));
    }
}