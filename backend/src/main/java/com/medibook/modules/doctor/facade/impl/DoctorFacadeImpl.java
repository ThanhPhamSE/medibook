package com.medibook.modules.doctor.facade.impl;

import org.springframework.stereotype.Service;

import com.medibook.modules.doctor.entity.Doctor;
import com.medibook.modules.doctor.facade.DoctorFacade;
import com.medibook.modules.doctor.service.DoctorService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DoctorFacadeImpl implements DoctorFacade {

    private final DoctorService doctorService;

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

}
