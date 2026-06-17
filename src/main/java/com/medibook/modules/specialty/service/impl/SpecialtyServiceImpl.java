package com.medibook.modules.specialty.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.medibook.common.exception.BadRequestException;
import com.medibook.common.exception.ResourceNotFoundException;
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
public class SpecialtyServiceImpl implements SpecialtyService {

    private final SpecialtyRepository specialtyRepository;
    private final SpecialtyMapper specialtyMapper;

    @Override
    public SpecialtyResponse create(SpecialtyCreateRequest request) {

        if (specialtyRepository.existsByNameAndDeletedAtIsNull(request.getName())) {
            throw new BadRequestException("Specialty already exists");
        }

        Specialty specialty = new Specialty();

        specialty.setName(request.getName());
        specialty.setDescription(request.getDescription());
        specialty.setCreatedAt(LocalDateTime.now());

        return specialtyMapper.toResponse(specialtyRepository.save(specialty));

    }

    @Override
    public SpecialtyResponse update(Long id, SpecialtyUpdateRequest request) {

        Specialty specialty = specialtyRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Specialty not found"));

        specialty.setName(request.getName());
        specialty.setDescription(request.getDescription());
        specialty.setUpdatedAt(LocalDateTime.now());

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
    public void delete(Long id) {

        Specialty specialty = specialtyRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Specialty not found"));

        specialty.setDeletedAt(LocalDateTime.now());

        specialtyRepository.save(specialty);

    }

}
