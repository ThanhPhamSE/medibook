package com.medibook.modules.doctor.facade;

import com.medibook.modules.doctor.entity.Doctor;

public interface DoctorFacade {

    Doctor getDoctorById(Long id);

    boolean exists(Long id);

}
