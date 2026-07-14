package com.medibook.modules.doctor.facade;

import java.util.List;
import java.util.Map;

import com.medibook.modules.doctor.entity.Doctor;

public interface DoctorFacade {

    boolean exists(Long id);

    Doctor getDoctorEntityById(Long id);

    Doctor getDoctorByUserId(Long userId);

    Doctor getDoctorForBooking(Long id);

    Map<Long, Long> countDoctorsBySpecialtyIds(List<Long> specialtyIds);
}
