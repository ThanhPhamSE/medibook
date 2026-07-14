package com.medibook.modules.specialty.controller;

import com.medibook.common.response.ApiResponse;
import com.medibook.common.response.PageResponse;
import com.medibook.modules.doctor.facade.DoctorFacade;
import com.medibook.modules.specialty.dto.response.SpecialtyResponse;
import com.medibook.modules.specialty.service.SpecialtyService;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/specialties")
@RequiredArgsConstructor
@Validated
public class SpecialtyController {

        private final SpecialtyService specialtyService;
        private final DoctorFacade doctorFacade;

        @GetMapping
        public ResponseEntity<ApiResponse<PageResponse<SpecialtyResponse>>> getAll(
                        @RequestParam(required = false) String keyword,
                        @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

                PageResponse<SpecialtyResponse> page = specialtyService.getAllByNameAndPage(keyword, pageable);

                List<Long> ids = page.getItems().stream().map(SpecialtyResponse::getId).toList();

                Map<Long, Long> countMap = doctorFacade.countDoctorsBySpecialtyIds(ids);

                page.getItems().forEach(s -> s.setDoctorCount(countMap.getOrDefault(s.getId(), 0L).intValue()));

                return ResponseEntity.ok(
                                ApiResponse.success(HttpStatus.OK.value(), "Get all specialties successfully", page));
        }

        @GetMapping("/{id}")
        public ResponseEntity<ApiResponse<SpecialtyResponse>> getById(
                        @PathVariable Long id) {

                SpecialtyResponse response = specialtyService.getById(id);

                return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(),
                                "Get specialty successfully",
                                response));
        }

}