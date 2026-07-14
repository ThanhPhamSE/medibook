package com.medibook.modules.schedule.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.medibook.common.enums.DayOfWeekEnum;
import com.medibook.common.exception.BadRequestException;
import com.medibook.modules.appointment.port.AppointmentSchedulePort;
import com.medibook.modules.doctor.entity.Doctor;
import com.medibook.modules.doctor.port.DoctorQueryPort;
import com.medibook.modules.schedule.business.ScheduleAvailabilityChecker;
import com.medibook.modules.schedule.business.SlotGenerator;
import com.medibook.modules.schedule.dto.request.SlotGenerateRequest;
import com.medibook.modules.schedule.dto.response.SlotResponse;
import com.medibook.modules.schedule.entity.DoctorTimeOff;
import com.medibook.modules.schedule.entity.DoctorWorkingPattern;
import com.medibook.modules.schedule.repository.DoctorTimeOffRepository;
import com.medibook.modules.schedule.repository.DoctorWorkingPatternRepository;
import com.medibook.modules.schedule.service.SlotGenerationService;

@Service
public class SlotGenerationServiceImpl implements SlotGenerationService {

    private final DoctorQueryPort doctorQueryPort;
    private final DoctorWorkingPatternRepository doctorWorkingPatternRepository;
    private final DoctorTimeOffRepository doctorTimeOffRepository;
    private final AppointmentSchedulePort appointmentSchedulePort;
    private final SlotGenerator slotGenerator;
    private final ScheduleAvailabilityChecker availabilityChecker;

    public SlotGenerationServiceImpl(DoctorQueryPort doctorQueryPort,
            DoctorWorkingPatternRepository doctorWorkingPatternRepository,
            DoctorTimeOffRepository doctorTimeOffRepository,
            @Lazy AppointmentSchedulePort appointmentSchedulePort,
            SlotGenerator slotGenerator,
            ScheduleAvailabilityChecker availabilityChecker) {
        this.doctorQueryPort = doctorQueryPort;
        this.doctorWorkingPatternRepository = doctorWorkingPatternRepository;
        this.doctorTimeOffRepository = doctorTimeOffRepository;
        this.appointmentSchedulePort = appointmentSchedulePort;
        this.slotGenerator = slotGenerator;
        this.availabilityChecker = availabilityChecker;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SlotResponse> generate(SlotGenerateRequest request) {

        Doctor doctor = doctorQueryPort.getDoctor(request.getDoctorId());

        LocalDate date = request.getDate();

        DayOfWeekEnum dayOfWeek = DayOfWeekEnum.fromJavaDayOfWeek(date.getDayOfWeek());

        DoctorWorkingPattern pattern = loadPattern(doctor.getId(), dayOfWeek);

        LocalDateTime workStart = LocalDateTime.of(date, pattern.getStartTime());

        LocalDateTime workEnd = LocalDateTime.of(date, pattern.getEndTime());

        List<DoctorTimeOff> timeOffs = loadTimeOffs(doctor.getId(), date);

        Set<LocalDateTime> bookedSlots = loadBookedSlots(doctor.getId(), date);

        return buildSlots(pattern, workStart, workEnd, timeOffs, bookedSlots);
    }

    private List<SlotResponse> buildSlots(DoctorWorkingPattern pattern, LocalDateTime workStart, LocalDateTime workEnd,
            List<DoctorTimeOff> timeOffs, Set<LocalDateTime> bookedSlots) {

        List<LocalDateTime> slotStarts = slotGenerator.generate(workStart, workEnd, pattern.getSlotDuration(),
                pattern.getBufferDuration() == null ? 0 : pattern.getBufferDuration());

        return slotStarts.stream()
                .map(slotStart -> {
                    LocalDateTime slotEnd = slotStart.plusMinutes(pattern.getSlotDuration());

                    boolean available = availabilityChecker.isAvailable(slotStart, slotEnd, timeOffs, bookedSlots);

                    return SlotResponse.builder().start(slotStart).end(slotEnd).available(available).build();
                }).toList();
    }

    private DoctorWorkingPattern loadPattern(Long doctorId, DayOfWeekEnum dayOfWeek) {

        return doctorWorkingPatternRepository.findByDoctorIdAndDayOfWeekAndDeletedAtIsNull(doctorId, dayOfWeek)
                .orElseThrow(() -> new BadRequestException("No working pattern found"));
    }

    private List<DoctorTimeOff> loadTimeOffs(Long doctorId, LocalDate date) {

        return doctorTimeOffRepository.findOverlappingDate(doctorId, date.atStartOfDay(),
                date.plusDays(1).atStartOfDay());
    }

    private Set<LocalDateTime> loadBookedSlots(Long doctorId, LocalDate date) {

        return appointmentSchedulePort.getBookedSlots(doctorId, date);
    }

}
