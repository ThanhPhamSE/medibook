package com.medibook.modules.medicalrecord.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.medibook.common.response.ApiResponse;
import com.medibook.modules.medicalrecord.dto.request.MedicalRecordCreateRequest;
import com.medibook.modules.medicalrecord.dto.request.MedicalRecordUpdateRequest;
import com.medibook.modules.medicalrecord.dto.response.MedicalRecordResponse;
import com.medibook.modules.medicalrecord.service.MedicalRecordService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/medical-records")
@RequiredArgsConstructor
public class MedicalRecordController {

    private final MedicalRecordService service;

    @PreAuthorize("hasRole('DOCTOR')")
    @PostMapping
    public ResponseEntity<ApiResponse<MedicalRecordResponse>> create(
            @Valid @RequestBody MedicalRecordCreateRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(service.create(request)));
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MedicalRecordResponse>> update(@PathVariable Long id,
            @Valid @RequestBody MedicalRecordUpdateRequest request) {

        return ResponseEntity.ok(ApiResponse.success(service.update(id, request)));
    }

    @PreAuthorize("hasAnyRole('PATIENT','DOCTOR','ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MedicalRecordResponse>> getById(@PathVariable Long id) {

        return ResponseEntity.ok(ApiResponse.success(service.getById(id)));
    }

    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Page<MedicalRecordResponse>>> getMyMedicalRecords(
            Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(service.getMyMedicalRecords(pageable)));
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {

        service.delete(id);

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/doctor")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<ApiResponse<Page<MedicalRecordResponse>>> getDoctorMedicalRecords(Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(service.getDoctorMedicalRecords(pageable)));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<MedicalRecordResponse>>> getAllMedicalRecords(Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(service.getAllMedicalRecords(pageable)));
    }
}
