package com.medibook.modules.doctor.facade.impl;

import org.springframework.stereotype.Service;

import com.medibook.modules.doctor.entity.Doctor;
import com.medibook.modules.doctor.facade.DoctorFacade;
import com.medibook.modules.doctor.mapper.DoctorMapper;
import com.medibook.modules.doctor.service.DoctorService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DoctorFacadeImpl implements DoctorFacade {

    private final DoctorService doctorService;

    @Override
    public Doctor getDoctorById(Long id) {

        return doctorService.getDoctorById(id);
    }

    @Override
    public boolean exists(Long doctorId) {
        return doctorService.exists(doctorId);
    }

}
