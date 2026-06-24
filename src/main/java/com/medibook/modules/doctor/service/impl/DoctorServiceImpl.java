package com.medibook.modules.doctor.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.medibook.common.constant.RoleConstants;
import com.medibook.common.exception.BadRequestException;
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
import com.medibook.modules.specialty.repository.SpecialtyRepository;
import com.medibook.modules.user.entity.Role;
import com.medibook.modules.user.entity.User;
import com.medibook.modules.user.repository.UserRepository;
import com.medibook.modules.user.service.RoleService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final SpecialtyRepository specialtyRepository;
    private final DoctorMapper doctorMapper;
    private final DoctorValidator validator;
    private final RoleService roleService;
    private final AuditService auditService;

    @Override
    public DoctorResponse createDoctor(CreateDoctorRequest request) {

        User user = getUser(request.getUserId());
        validator.validateCreateDoctor(user);

        Specialty specialty = getSpecialty(request.getSpecialtyId());

        Doctor doctor = doctorMapper.toEntity(request);
        doctor.setUser(user);
        doctor.setSpecialty(specialty);

        doctor = doctorRepository.save(doctor);
        doctor = getDoctorEntityById(doctor.getId());

        auditService.log("CREATE", "DOCTOR", doctor.getId(), null, doctor);

        return doctorMapper.toResponse(doctor);
    }

    @Override
    public DoctorResponse upgradeToDoctor(UpgradeToDoctorRequest request) {

        User user = getUser(request.getUserId());
        validator.validateUpgradeToDoctor(user);

        Specialty specialty = getSpecialty(request.getSpecialtyId());

        Role doctorRole = roleService.getDoctorRole();
        user.setRole(doctorRole);

        Doctor doctor = createDoctorProfile(user, specialty, request);

        doctor = doctorRepository.save(doctor);
        doctor = getDoctorEntityById(doctor.getId());

        auditService.log("UPGRADE", "DOCTOR", doctor.getId(), null, doctor);

        return doctorMapper.toResponse(doctor);
    }

    @Override
    @Transactional(readOnly = true)
    public DoctorResponse getDoctorById(Long id) {
        return doctorMapper.toResponse(getDoctor(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Doctor getDoctorEntityById(Long id) {
        return doctorRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DoctorSummaryResponse> searchDoctors(DoctorSearchRequest request, Pageable pageable) {

        validator.validateSearchRequest(request);

        Boolean active = request.getActive();
        if (active == null) {
            active = true;
        }

        Specification<Doctor> spec = DoctorSpecification.base()
                .and(DoctorSpecification.isNotDeleted())
                .and(DoctorSpecification.hasKeyword(request.getKeyword()))
                .and(DoctorSpecification.hasSpecialty(request.getSpecialtyId()))
                .and(DoctorSpecification.hasMinExperience(request.getMinExperience()))
                .and(DoctorSpecification.hasMaxExperience(request.getMaxExperience()))
                .and(DoctorSpecification.hasMinFee(request.getMinFee()))
                .and(DoctorSpecification.hasMaxFee(request.getMaxFee()))
                .and(DoctorSpecification.hasMinRating(request.getMinRating()))
                .and(DoctorSpecification.hasActiveStatus(active));

        return PageMapper.from(doctorRepository.findAll(spec, pageable).map(doctorMapper::toSummaryResponse));
    }

    @Override
    public DoctorResponse updateDoctor(Long id, UpdateDoctorRequest request) {

        Doctor doctor = getDoctor(id);

        Doctor oldSnapshot = buildSnapshot(doctor);

        doctorMapper.updateEntity(request, doctor);

        if (request.getSpecialtyId() != null) {
            doctor.setSpecialty(getSpecialty(request.getSpecialtyId()));
        }

        doctor = doctorRepository.save(doctor);
        doctor = getDoctorEntityById(doctor.getId());

        auditService.log("UPDATE", "DOCTOR", doctor.getId(), oldSnapshot, doctor);

        return doctorMapper.toResponse(doctor);
    }

    @Override
    public void deleteDoctor(Long id) {

        Doctor doctor = getDoctor(id);

        Doctor snapshot = buildSnapshot(doctor);

        doctor.setDeletedAt(LocalDateTime.now());
        doctorRepository.save(doctor);

        auditService.log("DELETE", "DOCTOR", doctor.getId(), snapshot, null);
    }

    private Doctor createDoctorProfile(User user, Specialty specialty, UpgradeToDoctorRequest request) {

        Doctor doctor = new Doctor();

        doctor.setUser(user);
        doctor.setSpecialty(specialty);
        doctor.setDegree(request.getDegree());
        doctor.setExperienceYears(request.getExperienceYears());
        doctor.setConsultationFee(request.getConsultationFee());
        doctor.setBiography(request.getBiography());

        doctor.setAverageRating(BigDecimal.ZERO);
        doctor.setTotalReviews(0);

        return doctor;
    }

    private Doctor buildSnapshot(Doctor doctor) {

        Doctor snapshot = new Doctor();
        snapshot.setId(doctor.getId());
        snapshot.setDegree(doctor.getDegree());
        snapshot.setExperienceYears(doctor.getExperienceYears());
        snapshot.setConsultationFee(doctor.getConsultationFee());
        snapshot.setBiography(doctor.getBiography());
        snapshot.setAverageRating(doctor.getAverageRating());
        snapshot.setTotalReviews(doctor.getTotalReviews());

        return snapshot;
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

    private User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Specialty getSpecialty(Long id) {
        return specialtyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Specialty not found"));
    }
}
