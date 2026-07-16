package com.medibook.modules.schedule.facade;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.medibook.modules.schedule.entity.DoctorWorkingPattern;

public interface ScheduleFacade {

    DoctorWorkingPattern getWorkingPattern(Long doctorId, LocalDateTime startTime);

    boolean isDoctorOnTimeOff(Long doctorId, LocalDateTime startDateTime, LocalDateTime endDateTime);

    void evictSlots(Long doctorId, LocalDate date);

}
