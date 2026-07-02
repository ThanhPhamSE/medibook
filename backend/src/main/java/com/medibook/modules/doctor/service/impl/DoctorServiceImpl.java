package com.medibook.modules.doctor.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.medibook.common.constant.RoleConstants;
import com.medibook.common.exception.BadRequestException;
import com.medibook.common.exception.ForbiddenException;
import com.medibook.common.exception.ResourceNotFoundException;
import com.medibook.common.response.PageResponse;
import com.medibook.common.response.util.PageMapper;
import com.medibook.modules.audit.service.AuditService;
import com.medibook.modules.doctor.dto.request.CreateDoctorRequest;
import com.medibook.modules.doctor.dto.request.DoctorSearchRequest;
import com.medibook.modules.doctor.dto.request.UpdateDoctorRequest;
import com.medibook.modules.doctor.dto.request.UpgradeToDoctorRequest;
import com.medibook.modules.doctor.dto.response.DoctorResponse;
import com.medibook.modules.doctor.dto.response.DoctorSummaryResponse;
import com.medibook.modules.doctor.entity.Doctor;
import com.medibook.modules.doctor.mapper.DoctorMapper;
import com.medibook.modules.doctor.repository.DoctorRepository;
import com.medibook.modules.doctor.service.DoctorService;
import com.medibook.modules.doctor.specification.DoctorSpecification;
import com.medibook.modules.doctor.validator.DoctorValidator;
import com.medibook.modules.specialty.entity.Specialty;
import com.medibook.modules.specialty.facade.SpecialtyFacade;
import com.medibook.modules.user.dto.internal.CurrentUserDto;
import com.medibook.modules.user.entity.User;
import com.medibook.modules.user.facade.UserFacade;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository doctorRepository;
    private final DoctorMapper doctorMapper;
    private final DoctorValidator validator;
    private final AuditService auditService;
    private final UserFacade userFacade;
    private final SpecialtyFacade specialtyFacade;

    @Override
    @Transactional
    public DoctorResponse createDoctor(CreateDoctorRequest request) {
        log.info("Request to create doctor profile for userId: {}", request.getUserId());

        User user = userFacade.getUserById(request.getUserId());

        if (user.getRole() == null || !RoleConstants.DOCTOR.equals(user.getRole().getName())) {
            log.warn("Failed to create doctor profile. User does not have DOCTOR role (userId={})",
                    request.getUserId());
            throw new BadRequestException("User must have DOCTOR role");
        }

        if (!user.getIsActive()) {
            log.warn("Failed to create doctor profile. User is inactive (userId={})", request.getUserId());
            throw new BadRequestException("User is inactive");
        }

        Specialty specialty = specialtyFacade.getSpecialtyById(request.getSpecialtyId());

        // Check for existing profile (active or soft-deleted)
        Doctor existing = doctorRepository.findByUserId(request.getUserId()).orElse(null);
        if (existing != null) {
            if (existing.getDeletedAt() == null) {
                log.warn("Failed to create doctor profile. Active profile already exists for userId: {}",
                        request.getUserId());
                throw new BadRequestException("Doctor profile already exists");
            } else {
                // Restore flow
                log.info("Restoring soft-deleted doctor profile (id={}) for userId: {}", existing.getId(),
                        request.getUserId());
                existing.setDeletedAt(null);
                existing.setSpecialty(specialty);
                existing.setDegree(request.getDegree());
                existing.setExperienceYears(request.getExperienceYears());
                existing.setConsultationFee(request.getConsultationFee());
                existing.setBiography(request.getBiography());

                Doctor saved = doctorRepository.save(existing);
                saved = getDoctorEntityById(saved.getId());

                log.info("Successfully restored soft-deleted doctor profile: id={}, userId={}", saved.getId(),
                        request.getUserId());
                auditService.log("RESTORE", "DOCTOR", saved.getId(), null, saved);

                return doctorMapper.toResponse(saved);
            }
        }

        Doctor doctor = doctorMapper.toEntity(request);
        doctor.setUser(user);
        doctor.setSpecialty(specialty);

        doctor = doctorRepository.save(doctor);
        doctor = getDoctorEntityById(doctor.getId());

        log.info("Successfully created doctor profile: id={}, userId={}", doctor.getId(), request.getUserId());
        auditService.log("CREATE", "DOCTOR", doctor.getId(), null, doctor);

        return doctorMapper.toResponse(doctor);
    }

    @Override
    @Transactional
    public DoctorResponse upgradeToDoctor(UpgradeToDoctorRequest request) {
        log.info("Request to upgrade user to doctor. userId: {}", request.getUserId());

        User user = userFacade.getUserById(request.getUserId());

        if (user.getRole() != null && RoleConstants.DOCTOR.equals(user.getRole().getName())) {
            log.warn("Failed to upgrade user to doctor. User is already a doctor (userId={})", request.getUserId());
            throw new BadRequestException("User is already a doctor");
        }

        if (!user.getIsActive()) {
            log.warn("Failed to upgrade user to doctor. User is inactive (userId={})", request.getUserId());
            throw new BadRequestException("User is inactive");
        }

        userFacade.upgradeToDoctor(request.getUserId());
        Specialty specialty = specialtyFacade.getSpecialtyById(request.getSpecialtyId());

        // Check for existing profile (active or soft-deleted)
        Doctor existing = doctorRepository.findByUserId(request.getUserId()).orElse(null);
        if (existing != null) {
            if (existing.getDeletedAt() == null) {
                log.warn("Failed to upgrade user. Active doctor profile already exists for userId: {}",
                        request.getUserId());
                throw new BadRequestException("Doctor profile already exists");
            } else {
                // Restore flow
                log.info("Restoring soft-deleted doctor profile (id={}) for upgraded user userId: {}", existing.getId(),
                        request.getUserId());
                existing.setDeletedAt(null);
                existing.setSpecialty(specialty);
                existing.setDegree(request.getDegree());
                existing.setExperienceYears(request.getExperienceYears() == null ? 0 : request.getExperienceYears());
                existing.setConsultationFee(
                        request.getConsultationFee() == null ? BigDecimal.ZERO : request.getConsultationFee());
                existing.setBiography(request.getBiography());

                Doctor saved = doctorRepository.save(existing);
                saved = getDoctorEntityById(saved.getId());

                log.info("Successfully restored soft-deleted doctor profile for upgraded user: id={}, userId={}",
                        saved.getId(), request.getUserId());
                auditService.log("RESTORE", "DOCTOR", saved.getId(), null, saved);

                return doctorMapper.toResponse(saved);
            }
        }

        Doctor doctor = createDoctorProfile(user, specialty, request);
        doctor = doctorRepository.save(doctor);
        doctor = getDoctorEntityById(doctor.getId());

        log.info("Successfully upgraded user and created doctor profile: id={}, userId={}", doctor.getId(),
                request.getUserId());
        auditService.log("UPGRADE", "DOCTOR", doctor.getId(), null, doctor);

        return doctorMapper.toResponse(doctor);
    }

    @Override
    public DoctorResponse getDoctorById(Long id) {
        return doctorMapper.toResponse(getDoctor(id));
    }

    @Override
    public Doctor getDoctorEntityById(Long id) {
        return doctorRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));
    }

    @Override
    public PageResponse<DoctorSummaryResponse> searchDoctors(DoctorSearchRequest request, Pageable pageable) {

        validator.validateSearchRequest(request);

        Specification<Doctor> spec = DoctorSpecification.isNotDeleted();

        if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
            spec = spec.and(DoctorSpecification.hasKeyword(request.getKeyword()));
        }

        if (request.getSpecialtyId() != null) {
            spec = spec.and(DoctorSpecification.hasSpecialty(request.getSpecialtyId()));
        }

        if (request.getMinExperience() != null) {
            spec = spec.and(DoctorSpecification.hasMinExperience(request.getMinExperience()));
        }

        if (request.getMaxExperience() != null) {
            spec = spec.and(DoctorSpecification.hasMaxExperience(request.getMaxExperience()));
        }

        if (request.getMinFee() != null) {
            spec = spec.and(DoctorSpecification.hasMinFee(request.getMinFee()));
        }

        if (request.getMaxFee() != null) {
            spec = spec.and(DoctorSpecification.hasMaxFee(request.getMaxFee()));
        }

        if (request.getMinRating() != null) {
            spec = spec.and(DoctorSpecification.hasMinRating(request.getMinRating()));
        }

        spec = spec.and(DoctorSpecification.hasActiveStatus(true));

        return PageMapper.from(doctorRepository.findAll(spec, pageable).map(doctorMapper::toSummaryResponse));
    }

    @Override
    @Transactional
    public DoctorResponse updateDoctor(Long id, UpdateDoctorRequest request) {
        log.info("Request to update doctor profile: id={}", id);

        Doctor doctor = getDoctor(id);

        // BOLA Check: Only allow updates by profile owner (Doctor themselves) or ADMIN
        CurrentUserDto currentUser = userFacade.getCurrentUser();
        if (!"ADMIN".equals(currentUser.getRole()) && !doctor.getUser().getId().equals(currentUser.getId())) {
            log.warn("Unauthorized profile update attempt on doctor ID: {} by current user ID: {}", id,
                    currentUser.getId());
            throw new ForbiddenException("You are not allowed to update this doctor profile");
        }

        Doctor oldSnapshot = doctorMapper.toSnapshot(doctor);

        doctorMapper.updateEntity(request, doctor);

        if (request.getSpecialtyId() != null) {
            doctor.setSpecialty(specialtyFacade.getSpecialtyById(request.getSpecialtyId()));
        }

        doctor = doctorRepository.save(doctor);
        doctor = getDoctorEntityById(doctor.getId());

        log.info("Successfully updated doctor profile: id={}", doctor.getId());
        auditService.log("UPDATE", "DOCTOR", doctor.getId(), oldSnapshot, doctor);

        return doctorMapper.toResponse(doctor);
    }

    @Override
    @Transactional
    public void deleteDoctor(Long id) {
        log.info("Request to delete doctor profile: id={}", id);

        Doctor doctor = getDoctor(id);

        Doctor snapshot = doctorMapper.toSnapshot(doctor);

        doctor.setDeletedAt(LocalDateTime.now());
        doctorRepository.save(doctor);

        log.info("Successfully soft-deleted doctor profile: id={}", id);
        auditService.log("DELETE", "DOCTOR", doctor.getId(), snapshot, null);
    }

    private Doctor createDoctorProfile(User user, Specialty specialty, UpgradeToDoctorRequest request) {

        Doctor doctor = doctorMapper.toEntity(request);

        doctor.setUser(user);
        doctor.setSpecialty(specialty);
        doctor.setAverageRating(BigDecimal.ZERO);
        doctor.setTotalReviews(0);

        return doctor;
    }

    @Override
    public Doctor getDoctorByUserId(Long userId) {
        return doctorRepository.findByUserIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with this account"));
    }

    private Doctor getDoctor(Long id) {
        return doctorRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));
    }

    @Override
    public boolean exists(Long doctorId) {

        return doctorRepository.existsById(doctorId);

    }
}
