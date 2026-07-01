package com.medibook.modules.specialty.controller.admin;

import com.medibook.common.response.ApiResponse;
import com.medibook.common.response.PageResponse;
import com.medibook.modules.specialty.dto.request.SpecialtyCreateRequest;
import com.medibook.modules.specialty.dto.request.SpecialtyUpdateRequest;
import com.medibook.modules.specialty.dto.response.SpecialtyResponse;
import com.medibook.modules.specialty.service.SpecialtyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/specialties")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasRole('ADMIN')")
public class SpecialtyAdminController {

    private final SpecialtyService specialtyService;

    @PostMapping
    public ResponseEntity<ApiResponse<SpecialtyResponse>> create(
            @Valid @RequestBody SpecialtyCreateRequest request) {

        SpecialtyResponse response = specialtyService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(),
                        "Create specialty successfully",
                        response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SpecialtyResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody SpecialtyUpdateRequest request) {

        SpecialtyResponse response = specialtyService.update(id, request);

        return ResponseEntity.ok(
                ApiResponse.success(HttpStatus.OK.value(),
                        "Update specialty successfully",
                        response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id) {

        specialtyService.delete(id);

        return ResponseEntity
                .ok(ApiResponse.success(HttpStatus.OK.value(),
                        "Delete specialty successfully",
                        null));
    }

    @PostMapping("/{id}/restore")
    public ResponseEntity<ApiResponse<SpecialtyResponse>> restore(@PathVariable Long id) {

        SpecialtyResponse response = specialtyService.restore(id);

        return ResponseEntity.ok(ApiResponse.success("Restore specialty successfully", response));
    }

    @GetMapping("/deleted")
    public ResponseEntity<ApiResponse<PageResponse<SpecialtyResponse>>> getDeleted(
            @PageableDefault(page = 0, size = 10, sort = "deletedAt", direction = Sort.Direction.DESC) Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(),
                "Get deleted specialties successfully",
                specialtyService.getDeleted(pageable)));
    }
}
