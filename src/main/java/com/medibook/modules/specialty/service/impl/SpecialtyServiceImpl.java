package com.medibook.modules.specialty.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.medibook.common.exception.BadRequestException;
import com.medibook.common.exception.ResourceNotFoundException;
import com.medibook.common.response.PageResponse;
import com.medibook.common.response.util.PageMapper;
import com.medibook.modules.specialty.dto.request.SpecialtyCreateRequest;
import com.medibook.modules.specialty.dto.request.SpecialtyUpdateRequest;
import com.medibook.modules.specialty.dto.response.SpecialtyResponse;
import com.medibook.modules.specialty.entity.Specialty;
import com.medibook.modules.specialty.mapper.SpecialtyMapper;
import com.medibook.modules.specialty.repository.SpecialtyRepository;
import com.medibook.modules.specialty.service.SpecialtyService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SpecialtyServiceImpl implements SpecialtyService {

    private final SpecialtyRepository specialtyRepository;
    private final SpecialtyMapper specialtyMapper;

    @Override
    @Transactional
    public SpecialtyResponse create(SpecialtyCreateRequest request) {

        if (specialtyRepository.existsByNameIgnoreCaseAndDeletedAtIsNull(request.getName().trim())) {
            throw new BadRequestException("Specialty already exists");
        }

        Specialty specialty = specialtyMapper.toEntityWhenCreate(request);

        return specialtyMapper.toResponse(specialtyRepository.save(specialty));

    }

    @Override
    @Transactional
    public SpecialtyResponse update(Long id, SpecialtyUpdateRequest request) {

        Specialty specialty = specialtyRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Specialty not found"));

        if (specialtyRepository.existsByNameIgnoreCaseAndDeletedAtIsNullAndIdNot(request.getName().trim(), id)) {
            throw new BadRequestException("Specialty already exists");
        }

        specialty = specialtyMapper.toEntityWhenUpdate(request);

        return specialtyMapper.toResponse(specialtyRepository.save(specialty));
    }

    @Override
    public List<SpecialtyResponse> getAll() {

        return specialtyRepository.findAllByDeletedAtIsNull().stream().map(specialtyMapper::toResponse).toList();
    }

    @Override
    public SpecialtyResponse getById(Long id) {

        Specialty specialty = specialtyRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Specialty not found"));

        return specialtyMapper.toResponse(specialty);
    }

    @Override
    @Transactional
    public void delete(Long id) {

        Specialty specialty = specialtyRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Specialty not found"));

        specialty.setDeletedAt(LocalDateTime.now());

        specialtyRepository.save(specialty);

    }

    @Override
    public PageResponse<SpecialtyResponse> getAllByPage(Pageable pageable) {

        Page<SpecialtyResponse> page = specialtyRepository.findAllByDeletedAtIsNull(pageable)
                .map(specialtyMapper::toResponse);

        return PageMapper.from(page);

    }

    @Override
    public PageResponse<SpecialtyResponse> getAllByNameAndPage(String keyword, Pageable pageable) {

        Page<SpecialtyResponse> page;

        if (keyword != null && !keyword.isBlank()) {

            page = specialtyRepository.findByDeletedAtIsNullAndNameContainingIgnoreCase(keyword, pageable)
                    .map(specialtyMapper::toResponse);

        } else {

            page = specialtyRepository.findAllByDeletedAtIsNull(pageable).map(specialtyMapper::toResponse);
        }

        return PageMapper.from(page);
    }

}
