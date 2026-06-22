package com.medibook.modules.schedule.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.medibook.common.enums.DayOfWeekEnum;
import com.medibook.common.exception.BadRequestException;
import com.medibook.common.exception.ResourceNotFoundException;
import com.medibook.modules.appointment.entity.Appointment;
import com.medibook.modules.appointment.service.AppointmentService;
import com.medibook.modules.doctor.entity.Doctor;
import com.medibook.modules.doctor.entity.DoctorTimeOff;
import com.medibook.modules.doctor.entity.DoctorWorkingPattern;
import com.medibook.modules.doctor.service.DoctorService;
import com.medibook.modules.schedule.business.ScheduleAvailabilityChecker;
import com.medibook.modules.schedule.business.SlotGenerator;
import com.medibook.modules.schedule.dto.request.SlotGenerateRequest;
import com.medibook.modules.schedule.dto.request.TimeOffRequest;
import com.medibook.modules.schedule.dto.request.WorkingPatternRequest;
import com.medibook.modules.schedule.dto.response.DoctorScheduleResponse;
import com.medibook.modules.schedule.dto.response.SlotResponse;
import com.medibook.modules.schedule.dto.response.TimeOffResponse;
import com.medibook.modules.schedule.dto.response.WorkingPatternResponse;
import com.medibook.modules.schedule.mapper.ScheduleMapper;
import com.medibook.modules.schedule.repository.DoctorTimeOffRepository;
import com.medibook.modules.schedule.repository.DoctorWorkingPatternRepository;
import com.medibook.modules.schedule.service.ScheduleService;
import com.medibook.modules.schedule.validator.TimeOffValidator;
import com.medibook.modules.schedule.validator.WorkingPatternValidator;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {

    private final DoctorWorkingPatternRepository doctorWorkingPatternRepository;
    private final DoctorTimeOffRepository doctorTimeOffRepository;
    private final DoctorService doctorService;
    private final WorkingPatternValidator workingPatternValidator;
    private final TimeOffValidator timeOffValidator;
    private final ScheduleMapper scheduleMapper;
    private final AppointmentService appointmentService;
    private final ScheduleAvailabilityChecker availabilityChecker;
    private final SlotGenerator slotGenerator;

    @Override
    public WorkingPatternResponse createWorkingPattern(WorkingPatternRequest request) {

        workingPatternValidator.validate(request);

        Doctor doctor = doctorService.getDoctorEntityById(request.getDoctorId());

        if (doctorWorkingPatternRepository.existsByDoctorIdAndDayOfWeekAndDeletedAtIsNull(doctor.getId(),
                request.getDayOfWeek())) {
            throw new BadRequestException("Working pattern already exists for this day");
        }

        DoctorWorkingPattern workingPattern = scheduleMapper.toEntity(request);

        workingPattern.setDoctor(doctor);

        workingPattern = doctorWorkingPatternRepository.save(workingPattern);

        return scheduleMapper.toResponse(workingPattern);
    }

    @Override
    public void deleteWorkingPattern(Long id) {

        DoctorWorkingPattern pattern = doctorWorkingPatternRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Working pattern not found"));

        pattern.setDeletedAt(LocalDateTime.now());

        doctorWorkingPatternRepository.save(pattern);
    }

    @Override
    public TimeOffResponse createTimeOff(TimeOffRequest request) {

        timeOffValidator.validate(request);

        Doctor doctor = doctorService.getDoctorEntityById(request.getDoctorId());

        List<DoctorTimeOff> overlaps = doctorTimeOffRepository.findOverlapping(doctor.getId(),
                request.getStartDatetime(), request.getEndDatetime());

        if (!overlaps.isEmpty()) {
            throw new BadRequestException("Time off overlaps existing time off");
        }

        DoctorTimeOff timeOff = scheduleMapper.toEntity(request);

        timeOff.setDoctor(doctor);

        timeOff = doctorTimeOffRepository.save(timeOff);

        return scheduleMapper.toResponse(timeOff);

    }

    @Override
    public void deleteTimeOff(Long id) {

        DoctorTimeOff doctorTimeOff = doctorTimeOffRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Time off not found"));

        doctorTimeOff.setDeletedAt(LocalDateTime.now());

        doctorTimeOffRepository.save(doctorTimeOff);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SlotResponse> generateSlots(SlotGenerateRequest request) {

        Doctor doctor = doctorService.getDoctorEntityById(request.getDoctorId());

        DayOfWeekEnum dayOfWeek = DayOfWeekEnum.valueOf(request.getDate().getDayOfWeek().name());

        DoctorWorkingPattern pattern = doctorWorkingPatternRepository
                .findByDoctorIdAndDayOfWeekAndDeletedAtIsNull(doctor.getId(), dayOfWeek)
                .orElseThrow(() -> new BadRequestException("No working pattern found"));

        if (pattern == null) {
            return List.of();
        }

        LocalDate date = request.getDate();

        LocalDateTime workStart = LocalDateTime.of(date, pattern.getStartTime());

        LocalDateTime workEnd = LocalDateTime.of(date, pattern.getEndTime());

        List<DoctorTimeOff> timeOffs = doctorTimeOffRepository.findByDoctorAndDate(doctor.getId(), date.atStartOfDay(),
                date.plusDays(1).atStartOfDay());

        Set<LocalDateTime> bookedSlots = appointmentService.getBookedAppointmentsByDate(doctor.getId(), date).stream()
                .map(Appointment::getStartDatetime).collect(Collectors.toSet());

        List<LocalDateTime> slotStarts = slotGenerator.generate(workStart, workEnd, pattern.getSlotDuration(),
                pattern.getBufferDuration() == null ? 0 : pattern.getBufferDuration());

        return slotStarts.stream().map(slotStart -> {
            LocalDateTime slotEnd = slotStart.plusMinutes(pattern.getSlotDuration());

            boolean available = availabilityChecker.isAvailable(slotStart, slotEnd, timeOffs, bookedSlots);

            return SlotResponse.builder().start(slotStart).end(slotEnd).available(available).build();
        }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DoctorScheduleResponse getDoctorSchedule(Long doctorId) {

        doctorService.getDoctorEntityById(doctorId);

        List<WorkingPatternResponse> patterns = doctorWorkingPatternRepository
                .findByDoctorIdAndDeletedAtIsNull(doctorId)
                .stream()
                .map(scheduleMapper::toResponse)
                .toList();

        List<TimeOffResponse> timeOffs = doctorTimeOffRepository
                .findByDoctorIdAndDeletedAtIsNull(doctorId)
                .stream()
                .map(scheduleMapper::toResponse)
                .toList();

        return DoctorScheduleResponse.builder()
                .doctorId(doctorId)
                .workingPartterns(patterns)
                .timeOffs(timeOffs)
                .build();
    }
}
