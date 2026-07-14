package com.medibook.modules.doctor.facade.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.medibook.modules.doctor.entity.Doctor;
import com.medibook.modules.doctor.facade.DoctorFacade;
import com.medibook.modules.doctor.repository.DoctorRepository;
import com.medibook.modules.doctor.repository.SpecialtyDoctorCountProjection;
import com.medibook.modules.doctor.service.DoctorService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DoctorFacadeImpl implements DoctorFacade {

    private final DoctorService doctorService;
    private final DoctorRepository doctorRepository;

    @Override
    public boolean exists(Long doctorId) {
        return doctorService.exists(doctorId);
    }

    @Override
    public Doctor getDoctorEntityById(Long id) {
        return doctorService.getDoctorEntityById(id);
    }

    @Override
    public Doctor getDoctorByUserId(Long userId) {
        return doctorService.getDoctorByUserId(userId);
    }

    public Doctor getDoctorForBooking(Long id) {

        return doctorRepository.findDoctorForBooking(id);

    }

    @Override
    public Map<Long, Long> countDoctorsBySpecialtyIds(List<Long> specialtyIds) {
        if (specialtyIds == null || specialtyIds.isEmpty()) {
            return Map.of();
        }
        return doctorRepository.countBySpecialtyIdIn(specialtyIds).stream()
                .collect(Collectors.toMap(
                        SpecialtyDoctorCountProjection::getSpecialtyId,
                        SpecialtyDoctorCountProjection::getCnt));
    }
}
