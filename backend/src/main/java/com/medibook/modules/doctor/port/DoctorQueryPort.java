package com.medibook.modules.doctor.port;

import com.medibook.modules.doctor.entity.Doctor;

public interface DoctorQueryPort {

    Doctor getDoctor(Long doctorId);

    boolean exists(Long doctorId);

}
