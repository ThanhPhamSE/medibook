package com.medibook.modules.doctor.adapter;

import org.springframework.stereotype.Component;

import com.medibook.modules.doctor.entity.Doctor;
import com.medibook.modules.doctor.port.DoctorQueryPort;
import com.medibook.modules.doctor.service.DoctorService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DoctorQueryAdapter implements DoctorQueryPort {

    private final DoctorService doctorService;

    @Override
    public Doctor getDoctor(Long doctorId) {
        return doctorService.getDoctorEntityById(doctorId);
    }

    @Override
    public boolean exists(Long doctorId) {
        try {
            doctorService.getDoctorEntityById(doctorId);
            return true;
        } catch (Exception e) {
            return false;
        }

    }
}
