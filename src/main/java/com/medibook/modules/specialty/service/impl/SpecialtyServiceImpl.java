package com.medibook.modules.specialty.service.impl;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.medibook.common.exception.BadRequestException;
import com.medibook.common.exception.ConflictException;
import com.medibook.common.exception.ResourceNotFoundException;
import com.medibook.common.response.PageResponse;
import com.medibook.common.response.util.PageMapper;
import com.medibook.modules.audit.service.AuditService;
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
    private final AuditService auditService;

    @Transactional
    public SpecialtyResponse create(SpecialtyCreateRequest request) {

        String name = request.getName().trim();
        String description = request.getDescription() == null ? null : request.getDescription().trim();

        Specialty existing = specialtyRepository.findForUpdateByName(name).orElse(null);

        // tồn tại active
        if (existing != null && existing.getDeletedAt() == null) {
            throw new ConflictException("Specialty already exists");
        }

        // CASE 2: tồn tại deleted
        if (existing != null && existing.getDeletedAt() != null) {
            existing.setDeletedAt(null);
            existing.setDescription(description);

            Specialty saved = specialtyRepository.save(existing);
            auditService.log("RESTORE", "SPECIALTY", saved.getId(), null, saved);

            return specialtyMapper.toResponse(saved);
        }

        // chưa tồn tại
        Specialty specialty = specialtyMapper.toEntity(request);
        specialty.setName(name);
        specialty.setDescription(description);

        Specialty saved = specialtyRepository.save(specialty);

        auditService.log("CREATE", "SPECIALTY", saved.getId(), null, saved);

        return specialtyMapper.toResponse(saved);
    }

    @Transactional
    public SpecialtyResponse update(Long id, SpecialtyUpdateRequest request) {
        validateId(id);

        String name = request.getName().trim();
        String description = request.getDescription() == null ? null : request.getDescription().trim();

        Specialty specialty = findSpecialtyById(id);

        Specialty conflict = specialtyRepository.findForUpdateByName(name).orElse(null);

        if (conflict != null
                && !conflict.getId().equals(id)
                && conflict.getDeletedAt() == null) {
            throw new ConflictException("Specialty already exists");
        }

        Specialty oldSnapshot = new Specialty();
        oldSnapshot.setId(specialty.getId());
        oldSnapshot.setName(specialty.getName());
        oldSnapshot.setDescription(specialty.getDescription());

        specialtyMapper.updateEntity(request, specialty);
        specialty.setName(name);
        specialty.setDescription(description);

        Specialty saved = specialtyRepository.save(specialty);

        auditService.log("UPDATE", "SPECIALTY", saved.getId(), oldSnapshot, saved);

        return specialtyMapper.toResponse(saved);
    }

    @Override
    public SpecialtyResponse getById(Long id) {
        validateId(id);

        Specialty specialty = findSpecialtyById(id);

        return specialtyMapper.toResponse(specialty);
    }

    @Transactional
    public void delete(Long id) {
        validateId(id);

        Specialty specialty = findSpecialtyById(id);

        specialty.setDeletedAt(LocalDateTime.now());

        specialtyRepository.save(specialty);

        auditService.log("DELETE", "SPECIALTY", id, specialty, null);
    }

    @Override
    public PageResponse<SpecialtyResponse> getAllByNameAndPage(String keyword, Pageable pageable) {

        Page<SpecialtyResponse> page;

        keyword = keyword == null ? null : keyword.trim();

        if (keyword != null && !keyword.isBlank()) {

            page = specialtyRepository.findByNameContainingIgnoreCaseAndDeletedAtIsNull(keyword, pageable)
                    .map(specialtyMapper::toResponse);

        } else {

            page = specialtyRepository.findByDeletedAtIsNull(pageable).map(specialtyMapper::toResponse);
        }

        return PageMapper.from(page);
    }

    @Transactional
    public SpecialtyResponse restore(Long id) {
        validateId(id);

        Specialty specialty = specialtyRepository.findByIdAndDeletedAtIsNotNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Deleted specialty not found"));

        Specialty conflict = specialtyRepository.findForUpdateByName(specialty.getName()).orElse(null);

        if (conflict != null && conflict.getDeletedAt() == null) {
            throw new ConflictException("Active specialty with same name exists");
        }

        specialty.setDeletedAt(null);

        Specialty saved = specialtyRepository.save(specialty);

        auditService.log("RESTORE", "SPECIALTY", id, null, saved);

        return specialtyMapper.toResponse(saved);
    }

    @Override
    public PageResponse<SpecialtyResponse> getDeleted(Pageable pageable) {
        Page<SpecialtyResponse> page = specialtyRepository.findByDeletedAtIsNotNull(pageable)
                .map(specialtyMapper::toResponse);

        return PageMapper.from(page);
    }

    private Specialty findSpecialtyById(Long id) {
        return specialtyRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Specialty not found"));
    }

    @Override
    public Specialty getEntityById(Long id) {
        validateId(id);
        return findSpecialtyById(id);
    }

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new BadRequestException("ID must be a positive number");
        }
    }
}
