package com.medibook.modules.schedule.business;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.medibook.modules.doctor.entity.DoctorTimeOff;

@Component
public class ScheduleAvailabilityChecker {

    public boolean isAvailable(LocalDateTime slotStart, LocalDateTime slotEnd, List<DoctorTimeOff> timeOffs,
            Set<LocalDateTime> bookedSlots) {

        return !isTimeOff(slotStart, slotEnd, timeOffs) && !bookedSlots.contains(slotStart);
    }

    private boolean isTimeOff(LocalDateTime slotStart, LocalDateTime slotEnd, List<DoctorTimeOff> timeOffs) {

        return timeOffs.stream()
                .anyMatch(off -> overlap(slotStart, slotEnd, off.getStartDatetime(), off.getEndDatetime()));
    }

    private boolean overlap(LocalDateTime s1, LocalDateTime e1, LocalDateTime s2, LocalDateTime e2) {

        return s1.isBefore(e2) && e1.isAfter(s2);
    }
}