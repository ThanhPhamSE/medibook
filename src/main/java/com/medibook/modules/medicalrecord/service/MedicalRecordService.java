package com.medibook.modules.medicalrecord.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.medibook.modules.medicalrecord.dto.request.MedicalRecordCreateRequest;
import com.medibook.modules.medicalrecord.dto.request.MedicalRecordUpdateRequest;
import com.medibook.modules.medicalrecord.dto.response.MedicalRecordResponse;

public interface MedicalRecordService {

    MedicalRecordResponse create(MedicalRecordCreateRequest request);

    MedicalRecordResponse update(Long id, MedicalRecordUpdateRequest request);

    MedicalRecordResponse getById(Long id);

    Page<MedicalRecordResponse> getMyMedicalRecords(Pageable pageable);

    void delete(Long id);

    Page<MedicalRecordResponse> getDoctorMedicalRecords(Pageable pageable);

    Page<MedicalRecordResponse> getAllMedicalRecords(Pageable pageable);
}