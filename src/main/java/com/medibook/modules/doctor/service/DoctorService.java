package com.medibook.modules.doctor.service;

import org.springframework.data.domain.Pageable;

import com.medibook.common.response.PageResponse;
import com.medibook.modules.doctor.dto.request.CreateDoctorRequest;
import com.medibook.modules.doctor.dto.request.DoctorSearchRequest;
import com.medibook.modules.doctor.dto.request.UpdateDoctorRequest;
import com.medibook.modules.doctor.dto.request.UpgradeToDoctorRequest;
import com.medibook.modules.doctor.dto.response.DoctorResponse;
import com.medibook.modules.doctor.dto.response.DoctorSummaryResponse;

public interface DoctorService {

    DoctorResponse createDoctor(CreateDoctorRequest request);

    DoctorResponse getDoctorById(Long id);

    PageResponse<DoctorSummaryResponse> searchDoctors(DoctorSearchRequest request, Pageable pageable);

    DoctorResponse updateDoctor(Long id, UpdateDoctorRequest request);

    void deleteDoctor(Long id);

    DoctorResponse upgradeToDoctor(UpgradeToDoctorRequest request);
}
