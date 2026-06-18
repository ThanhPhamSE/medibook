package com.medibook.modules.specialty.controller;

import com.medibook.common.response.ApiResponse;
import com.medibook.modules.specialty.dto.request.SpecialtyCreateRequest;
import com.medibook.modules.specialty.dto.request.SpecialtyUpdateRequest;
import com.medibook.modules.specialty.dto.response.SpecialtyResponse;
import com.medibook.modules.specialty.service.SpecialtyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/specialties")
@RequiredArgsConstructor
public class SpecialtyController {

        private final SpecialtyService specialtyService;

        @PostMapping
        public ResponseEntity<ApiResponse<SpecialtyResponse>> create(
                        @Valid @RequestBody SpecialtyCreateRequest request) {

                SpecialtyResponse response = specialtyService.create(request);

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(ApiResponse.success(
                                                HttpStatus.CREATED.value(),
                                                "Create specialty successfully",
                                                response));
        }

        @GetMapping
        public ResponseEntity<ApiResponse<List<SpecialtyResponse>>> getAll() {

                List<SpecialtyResponse> response = specialtyService.getAll();

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                HttpStatus.OK.value(),
                                                "Get all specialties successfully",
                                                response));
        }

        @GetMapping("/{id}")
        public ResponseEntity<ApiResponse<SpecialtyResponse>> getById(@PathVariable Long id) {

                SpecialtyResponse response = specialtyService.getById(id);

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                HttpStatus.OK.value(),
                                                "Get specialty successfully",
                                                response));
        }

        @PutMapping("/{id}")
        public ResponseEntity<ApiResponse<SpecialtyResponse>> update(
                        @PathVariable Long id,
                        @Valid @RequestBody SpecialtyUpdateRequest request) {

                SpecialtyResponse response = specialtyService.update(id, request);

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                HttpStatus.OK.value(),
                                                "Update specialty successfully",
                                                response));
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {

                specialtyService.delete(id);

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                HttpStatus.OK.value(),
                                                "Delete specialty successfully",
                                                null));
        }

        @GetMapping("/getbypage")
        public ResponseEntity<ApiResponse<?>> getAll(@RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {

                Pageable pageable = PageRequest.of(page, size);

                return ResponseEntity.ok(ApiResponse.success("Get specialties successful",
                                specialtyService.getAllByPage(pageable)));
        }

        @GetMapping("getallbynamepage")
        public ResponseEntity<ApiResponse<?>> getAll(

                        @RequestParam(required = false) String keyword,
                        @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

                return ResponseEntity.ok(ApiResponse.success(specialtyService.getAllByNameAndPage(
                                keyword,
                                pageable)));
        }

}