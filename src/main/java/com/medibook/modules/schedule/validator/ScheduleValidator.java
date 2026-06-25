package com.medibook.modules.schedule.validator;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Component;

import com.medibook.common.enums.DayOfWeekEnum;
import com.medibook.common.exception.BadRequestException;
import com.medibook.modules.schedule.entity.DoctorTimeOff;
import com.medibook.modules.schedule.entity.DoctorWorkingPattern;
import com.medibook.modules.schedule.repository.DoctorTimeOffRepository;
import com.medibook.modules.schedule.repository.DoctorWorkingPatternRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ScheduleValidator {

    private final DoctorWorkingPatternRepository workingPatternRepository;
    private final DoctorTimeOffRepository timeOffRepository;

    public DoctorWorkingPattern validateDoctorWorkingPattern(Long doctorId, LocalDateTime startDatetime) {

        DayOfWeekEnum dayOfWeek = DayOfWeekEnum.valueOf(startDatetime.getDayOfWeek().name());

        DoctorWorkingPattern pattern = workingPatternRepository
                .findByDoctorIdAndDayOfWeekAndDeletedAtIsNull(doctorId, dayOfWeek)
                .orElseThrow(() -> new BadRequestException("Doctor does not work in this day"));

        LocalTime start = startDatetime.toLocalTime();

        LocalDateTime endDatetime = startDatetime.plusMinutes(pattern.getSlotDuration());

        // Working hours
        if (start.isBefore(pattern.getStartTime()) || endDatetime.toLocalTime().isAfter(pattern.getEndTime())) {
            throw new BadRequestException("Outside working hours");
        }

        // Slot alignment
        int buffer = pattern.getBufferDuration() == null ? 0 : pattern.getBufferDuration();

        int interval = pattern.getSlotDuration() + buffer;

        long diff = Duration.between(pattern.getStartTime(), start).toMinutes();

        if (diff % interval != 0) {
            throw new BadRequestException("Invalid slot time");
        }

        // time off check
        LocalDate date = startDatetime.toLocalDate();

        List<DoctorTimeOff> timeOffs = timeOffRepository.findOverlappingDate(doctorId, date.atStartOfDay(),
                date.plusDays(1).atStartOfDay());

        for (DoctorTimeOff off : timeOffs) {
            if (overlap(startDatetime, endDatetime, off.getStartDatetime(), off.getEndDatetime())) {
                throw new BadRequestException("Doctor is on time off");
            }
        }

        return pattern;
    }

    private boolean overlap(LocalDateTime s1, LocalDateTime e1, LocalDateTime s2, LocalDateTime e2) {

        return s1.isBefore(e2) && e1.isAfter(s2);

    }

}
