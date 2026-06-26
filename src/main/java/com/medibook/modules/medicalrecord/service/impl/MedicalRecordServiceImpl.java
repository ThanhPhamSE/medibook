package com.medibook.modules.medicalrecord.service.impl;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.medibook.common.constant.RoleConstants;
import com.medibook.common.exception.ResourceNotFoundException;
import com.medibook.modules.appointment.entity.Appointment;
import com.medibook.modules.appointment.repository.AppointmentRepository;
import com.medibook.modules.appointment.service.AppointmentService;
import com.medibook.modules.medicalrecord.dto.request.MedicalRecordCreateRequest;
import com.medibook.modules.medicalrecord.dto.request.MedicalRecordUpdateRequest;
import com.medibook.modules.medicalrecord.dto.response.MedicalRecordResponse;
import com.medibook.modules.medicalrecord.entity.MedicalRecord;
import com.medibook.modules.medicalrecord.mapper.MedicalRecordMapper;
import com.medibook.modules.medicalrecord.repository.MedicalRecordRepository;
import com.medibook.modules.medicalrecord.service.MedicalRecordService;
import com.medibook.modules.medicalrecord.validator.MedicalRecordSecurityValidator;
import com.medibook.modules.medicalrecord.validator.MedicalRecordValidator;
import com.medibook.modules.user.entity.User;
import com.medibook.modules.user.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class MedicalRecordServiceImpl implements MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final MedicalRecordMapper medicalRecordMapper;
    private final MedicalRecordValidator validator;
    private final MedicalRecordSecurityValidator securityValidator;

    private final AppointmentService appointmentService;

    private final UserService userService;

    @Override
    public MedicalRecordResponse create(MedicalRecordCreateRequest request) {

        Appointment appointment = appointmentService.getAppointmentEntity(request.getAppointmentId());

        validator.validateCreate(appointment.getId(), appointment.getStatus(),
                medicalRecordRepository.existsByAppointmentId(appointment.getId()));

        User user = userService.getCurrentUser();

        securityValidator.validateDoctorOwnership(appointment, user);

        MedicalRecord medicalRecord = new MedicalRecord();

        medicalRecord.setAppointment(appointment);

        medicalRecord.setDiagnosis(request.getDiagnosis());

        medicalRecord.setPrescription(request.getPrescription());

        medicalRecord.setNote(request.getNote());

        medicalRecord = medicalRecordRepository.save(medicalRecord);

        return medicalRecordMapper.toResponse(medicalRecord);
    }

    @Override
    public MedicalRecordResponse update(Long id, MedicalRecordUpdateRequest request) {

        MedicalRecord medicalRecord = medicalRecordRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medical record not found"));

        User user = userService.getCurrentUser();

        securityValidator.validateDoctorOwnership(medicalRecord.getAppointment(), user);

        medicalRecord.setDiagnosis(request.getDiagnosis());
        medicalRecord.setPrescription(request.getPrescription());
        medicalRecord.setNote(request.getNote());

        medicalRecordRepository.save(medicalRecord);

        return medicalRecordMapper.toResponse(medicalRecord);
    }

    @Override
    @Transactional(readOnly = true)
    public MedicalRecordResponse getById(Long id) {

        MedicalRecord medicalRecord = medicalRecordRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medical record not found"));

        User currentUser = userService.getCurrentUser();

        boolean isAdmin = RoleConstants.ADMIN.equals(currentUser.getRole().getName());

        securityValidator.validateViewPermission(medicalRecord, currentUser, isAdmin);

        return medicalRecordMapper.toResponse(medicalRecord);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MedicalRecordResponse> getMyMedicalRecords(Pageable pageable) {

        Long patientId = userService.getCurrentUser().getId();

        return medicalRecordRepository.findByAppointmentPatientIdAndDeletedAtIsNull(patientId, pageable)
                .map(medicalRecordMapper::toResponse);
    }

    @Override
    public void delete(Long id) {

        MedicalRecord medicalRecord = medicalRecordRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medical record not found"));

        User currentUser = userService.getCurrentUser();

        securityValidator.validateDoctorOwnership(medicalRecord.getAppointment(), currentUser);

        medicalRecord.setDeletedAt(LocalDateTime.now());

        medicalRecordRepository.save(medicalRecord);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MedicalRecordResponse> getDoctorMedicalRecords(Pageable pageable) {

        Long doctorUserId = userService.getCurrentUser().getId();

        return medicalRecordRepository.findByAppointmentDoctorUserIdAndDeletedAtIsNull(doctorUserId, pageable)
                .map(medicalRecordMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MedicalRecordResponse> getAllMedicalRecords(Pageable pageable) {

        return medicalRecordRepository.findByDeletedAtIsNull(pageable).map(medicalRecordMapper::toResponse);
    }
}
