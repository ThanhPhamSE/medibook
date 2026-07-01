package com.medibook.modules.schedule.facade.impl;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.medibook.modules.schedule.entity.DoctorWorkingPattern;
import com.medibook.modules.schedule.facade.ScheduleFacade;
import com.medibook.modules.schedule.validator.ScheduleValidator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScheduleFacadeImpl implements ScheduleFacade {

    private final ScheduleValidator scheduleValidator;

    @Override
    public DoctorWorkingPattern getWorkingPattern(Long doctorId, LocalDateTime startTime) {

        return scheduleValidator.validateDoctorWorkingPattern(doctorId, startTime);
    }

}
