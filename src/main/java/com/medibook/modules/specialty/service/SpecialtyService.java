package com.medibook.modules.specialty.service;

import org.springframework.data.domain.Pageable;

import com.medibook.common.response.PageResponse;
import com.medibook.modules.specialty.dto.request.SpecialtyCreateRequest;
import com.medibook.modules.specialty.dto.request.SpecialtyUpdateRequest;
import com.medibook.modules.specialty.dto.response.SpecialtyResponse;

public interface SpecialtyService {

    SpecialtyResponse create(SpecialtyCreateRequest request);

    SpecialtyResponse update(Long id, SpecialtyUpdateRequest request);

    SpecialtyResponse getById(Long id);

    void delete(Long id);

    PageResponse<SpecialtyResponse> getAllByNameAndPage(String keyword, Pageable pageable);

    SpecialtyResponse restore(Long id);

    PageResponse<SpecialtyResponse> getDeleted(Pageable pageable);
}
