package com.medibook.modules.schedule.facade.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.medibook.modules.schedule.entity.DoctorWorkingPattern;
import com.medibook.modules.schedule.entity.DoctorTimeOff;
import com.medibook.modules.schedule.facade.ScheduleFacade;
import com.medibook.modules.schedule.repository.DoctorTimeOffRepository;
import com.medibook.modules.schedule.validator.ScheduleValidator;
import com.medibook.modules.schedule.cache.ScheduleCacheService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScheduleFacadeImpl implements ScheduleFacade {

    private final ScheduleValidator scheduleValidator;
    private final DoctorTimeOffRepository timeOffRepository;
    private final ScheduleCacheService scheduleCacheService;

    @Override
    public DoctorWorkingPattern getWorkingPattern(Long doctorId, LocalDateTime startTime) {

        return scheduleValidator.validateDoctorWorkingPattern(doctorId, startTime);
    }

    @Override
    public boolean isDoctorOnTimeOff(Long doctorId, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        List<DoctorTimeOff> overlappingTimeOffs = timeOffRepository.findOverlapping(
                doctorId, startDateTime, endDateTime);
        return !overlappingTimeOffs.isEmpty();
    }

    @Override
    public void evictSlots(Long doctorId, LocalDate date) {
        scheduleCacheService.evictSlots(doctorId, date);
    }

}
