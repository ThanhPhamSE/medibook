package com.medibook.modules.doctor.facade;

import com.medibook.modules.doctor.entity.Doctor;

public interface DoctorFacade {

    boolean exists(Long id);

    Doctor getDoctorEntityById(Long id);

    Doctor getDoctorByUserId(Long userId);
}
