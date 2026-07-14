package com.medibook.modules.schedule.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.medibook.common.constant.RoleConstants;
import com.medibook.common.enums.DayOfWeekEnum;
import com.medibook.common.exception.BadRequestException;
import com.medibook.common.exception.ForbiddenException;
import com.medibook.common.exception.ResourceNotFoundException;
import com.medibook.modules.doctor.entity.Doctor;

import com.medibook.modules.schedule.cache.ScheduleCacheService;
import com.medibook.modules.schedule.dto.request.SlotGenerateRequest;
import com.medibook.modules.schedule.dto.request.TimeOffRequest;
import com.medibook.modules.schedule.dto.request.TimeOffUpdateRequest;
import com.medibook.modules.schedule.dto.request.WorkingPatternRequest;
import com.medibook.modules.schedule.dto.request.WorkingPatternUpdateRequest;
import com.medibook.modules.schedule.dto.response.DoctorScheduleResponse;
import com.medibook.modules.schedule.dto.response.SlotResponse;
import com.medibook.modules.schedule.dto.response.TimeOffResponse;
import com.medibook.modules.schedule.dto.response.WorkingPatternResponse;
import com.medibook.modules.schedule.entity.DoctorTimeOff;
import com.medibook.modules.schedule.entity.DoctorWorkingPattern;
import com.medibook.modules.schedule.mapper.ScheduleMapper;
import com.medibook.modules.schedule.repository.DoctorTimeOffRepository;
import com.medibook.modules.schedule.repository.DoctorWorkingPatternRepository;
import com.medibook.modules.schedule.service.ScheduleService;

import com.medibook.modules.schedule.validator.TimeOffValidator;
import com.medibook.modules.schedule.validator.WorkingPatternValidator;

import com.medibook.modules.appointment.port.AppointmentSchedulePort;
import com.medibook.modules.doctor.port.DoctorQueryPort;
import com.medibook.security.util.SecurityUtils;
import com.medibook.modules.audit.service.AuditService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ScheduleServiceImpl implements ScheduleService {

    private final DoctorWorkingPatternRepository doctorWorkingPatternRepository;
    private final DoctorTimeOffRepository doctorTimeOffRepository;
    private final WorkingPatternValidator workingPatternValidator;
    private final TimeOffValidator timeOffValidator;
    private final ScheduleMapper scheduleMapper;
    private final AppointmentSchedulePort appointmentSchedulePort;
    private final DoctorQueryPort doctorQueryPort;
    private final ScheduleCacheService scheduleCacheService;
    private final AuditService auditService;

    @Override
    public WorkingPatternResponse createWorkingPattern(WorkingPatternRequest request) {
        log.info("Request to create working pattern: doctorId={}, dayOfWeek={}", request.getDoctorId(),
                request.getDayOfWeek());

        workingPatternValidator.validate(request);
        ensureScheduleAccess(request.getDoctorId());

        Doctor doctor = doctorQueryPort.getDoctor(request.getDoctorId());

        // Already enforced: one working pattern per day-of-week per doctor.
        // If a pattern already exists for this day, reject instead of creating
        // a duplicate/conflicting one.
        if (doctorWorkingPatternRepository.existsByDoctorIdAndDayOfWeekAndDeletedAtIsNull(doctor.getId(),
                request.getDayOfWeek())) {
            throw new BadRequestException("Working pattern already exists for this day");
        }

        DoctorWorkingPattern workingPattern = scheduleMapper.toEntity(request);

        workingPattern.setDoctor(doctor);

        workingPattern = doctorWorkingPatternRepository.save(workingPattern);

        clearSlotCacheSafely("creating working pattern");

        auditService.log("CREATE", "WorkingPattern", workingPattern.getId(), null, workingPattern);

        log.info("Successfully created working pattern: id={}, doctorId={}", workingPattern.getId(), doctor.getId());

        return scheduleMapper.toResponse(workingPattern);
    }

    @Override
    public WorkingPatternResponse updateWorkingPattern(Long id, WorkingPatternUpdateRequest request) {
        log.info("Request to update working pattern: id={}", id);

        DoctorWorkingPattern pattern = doctorWorkingPatternRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Working pattern not found"));

        ensureScheduleAccess(pattern);
        workingPatternValidator.validateUpdate(pattern, request);

        if (appointmentSchedulePort.hasFutureAppointments(pattern.getDoctor().getId(), LocalDateTime.now())) {
            throw new BadRequestException("Cannot update working pattern because future appointments exist");
        }

        pattern.setDayOfWeek(request.getDayOfWeek());
        pattern.setStartTime(request.getStartTime());
        pattern.setEndTime(request.getEndTime());
        pattern.setSlotDuration(request.getSlotDuration());
        pattern.setBufferDuration(request.getBufferDuration());

        doctorWorkingPatternRepository.save(pattern);

        clearSlotCacheSafely("updating working pattern");

        auditService.log("UPDATE", "WorkingPattern", pattern.getId(), null, pattern);

        log.info("Successfully updated working pattern: id={}", id);

        return scheduleMapper.toResponse(pattern);
    }

    @Override
    public void deleteWorkingPattern(Long id) {
        log.info("Request to delete working pattern: id={}", id);

        DoctorWorkingPattern pattern = doctorWorkingPatternRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Working pattern not found"));

        ensureScheduleAccess(pattern);

        if (appointmentSchedulePort.hasFutureAppointments(pattern.getDoctor().getId(), LocalDateTime.now())) {
            throw new BadRequestException("Cannot delete working pattern because future appointments exist");
        }

        pattern.setDeletedAt(LocalDateTime.now());

        doctorWorkingPatternRepository.save(pattern);

        clearSlotCacheSafely("deleting working pattern");

        auditService.log("DELETE", "WorkingPattern", pattern.getId(), pattern, null);

        log.info("Successfully deleted working pattern: id={}", id);
    }

    @Override
    public TimeOffResponse createTimeOff(TimeOffRequest request) {
        log.info("Request to create time off: doctorId={}, start={}, end={}", request.getDoctorId(),
                request.getStartDatetime(), request.getEndDatetime());

        timeOffValidator.validate(request);
        ensureScheduleAccess(request.getDoctorId());

        Doctor doctor = doctorQueryPort.getDoctor(request.getDoctorId());

        // Rule 1: if the doctor has no working pattern at all for this day of
        // week, there's nothing to "take off" — throw exception to inform user
        DayOfWeekEnum dayOfWeek = DayOfWeekEnum.fromJavaDayOfWeek(request.getStartDatetime().getDayOfWeek());
        boolean hasWorkingPatternThatDay = doctorWorkingPatternRepository
                .existsByDoctorIdAndDayOfWeekAndDeletedAtIsNull(doctor.getId(), dayOfWeek);

        if (!hasWorkingPatternThatDay) {
            log.info("Doctor {} has no working pattern on {}; cannot create time off",
                    doctor.getId(), dayOfWeek);
            throw new BadRequestException("Bác sĩ không có lịch làm việc vào ngày này");
        }

        List<DoctorTimeOff> overlaps = doctorTimeOffRepository.findOverlapping(doctor.getId(),
                request.getStartDatetime(), request.getEndDatetime());

        // Rule 2: an exact duplicate (same start AND same end) already exists —
        // treat this as idempotent and return the existing record instead of
        // creating a redundant copy.
        DoctorTimeOff exactDuplicate = overlaps.stream()
                .filter(o -> o.getStartDatetime().isEqual(request.getStartDatetime())
                        && o.getEndDatetime().isEqual(request.getEndDatetime()))
                .findFirst()
                .orElse(null);

        if (exactDuplicate != null) {
            log.info("Identical time off already exists (id={}) for doctor {}; skipping creation",
                    exactDuplicate.getId(), doctor.getId());
            return scheduleMapper.toResponse(exactDuplicate);
        }

        // Rule 3: partial overlap(s) with existing time off(s) — merge them all
        // into a single widened record instead of rejecting as a conflict.
        // e.g. 12:00-12:30 and 12:25-12:45 both being "off" means the doctor is
        // simply off from 12:00-12:45; there's no reason to keep two rows or
        // to block the second request.
        if (!overlaps.isEmpty()) {
            LocalDateTime mergedStart = request.getStartDatetime();
            LocalDateTime mergedEnd = request.getEndDatetime();

            for (DoctorTimeOff existing : overlaps) {
                if (existing.getStartDatetime().isBefore(mergedStart)) {
                    mergedStart = existing.getStartDatetime();
                }
                if (existing.getEndDatetime().isAfter(mergedEnd)) {
                    mergedEnd = existing.getEndDatetime();
                }
            }

            for (DoctorTimeOff existing : overlaps) {
                existing.setDeletedAt(LocalDateTime.now());
                doctorTimeOffRepository.save(existing);
            }

            DoctorTimeOff merged = new DoctorTimeOff();
            merged.setDoctor(doctor);
            merged.setStartDatetime(mergedStart);
            merged.setEndDatetime(mergedEnd);
            merged.setReason(request.getReason());
            merged = doctorTimeOffRepository.save(merged);

            clearSlotCacheSafely("creating time off (merged)");

            auditService.log("CREATE", "TimeOff", merged.getId(), overlaps, merged);

            log.info("Merged {} overlapping time off(s) into id={} ({} - {})", overlaps.size(), merged.getId(),
                    mergedStart, mergedEnd);

            return scheduleMapper.toResponse(merged);
        }

        // No conflicts of any kind — plain create.
        DoctorTimeOff timeOff = scheduleMapper.toEntity(request);

        timeOff.setDoctor(doctor);

        timeOff = doctorTimeOffRepository.save(timeOff);

        clearSlotCacheSafely("creating time off");

        auditService.log("CREATE", "TimeOff", timeOff.getId(), null, timeOff);

        log.info("Successfully created time off: id={}, doctorId={}", timeOff.getId(), doctor.getId());

        return scheduleMapper.toResponse(timeOff);
    }

    @Override
    public TimeOffResponse updateTimeOff(Long id, TimeOffUpdateRequest request) {
        log.info("Request to update time off: id={}", id);

        DoctorTimeOff timeOff = doctorTimeOffRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Time off not found"));

        ensureScheduleAccess(timeOff);

        LocalDateTime now = LocalDateTime.now();
        if (timeOff.getStartDatetime().isBefore(now) && timeOff.getEndDatetime().isAfter(now)) {
            throw new BadRequestException("Cannot update ongoing time off");
        }

        timeOff.setStartDatetime(request.getStartDatetime());
        timeOff.setEndDatetime(request.getEndDatetime());
        timeOff.setReason(request.getReason());

        doctorTimeOffRepository.save(timeOff);

        clearSlotCacheSafely("updating time off");

        auditService.log("UPDATE", "TimeOff", timeOff.getId(), null, timeOff);

        log.info("Successfully updated time off: id={}", id);

        return scheduleMapper.toResponse(timeOff);
    }

    @Override
    public void deleteTimeOff(Long id) {
        log.info("Request to delete time off: id={}", id);

        DoctorTimeOff doctorTimeOff = doctorTimeOffRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Time off not found"));

        ensureScheduleAccess(doctorTimeOff);

        LocalDateTime now = LocalDateTime.now();

        if (doctorTimeOff.getStartDatetime().isBefore(now)
                &&
                doctorTimeOff.getEndDatetime().isAfter(now)) {
            throw new BadRequestException(
                    "Cannot delete ongoing time off");
        }

        doctorTimeOff.setDeletedAt(LocalDateTime.now());

        doctorTimeOffRepository.save(doctorTimeOff);

        clearSlotCacheSafely("deleting time off");

        auditService.log("DELETE", "TimeOff", doctorTimeOff.getId(), doctorTimeOff, null);

        log.info("Successfully deleted time off: id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SlotResponse> generateSlots(SlotGenerateRequest request) {

        return scheduleCacheService.getSlots(request.getDoctorId(), request.getDate());

    }

    @Override
    @Transactional(readOnly = true)
    public DoctorScheduleResponse getDoctorSchedule(Long doctorId) {

        // Allow all authenticated users to view doctor schedules for booking purposes
        doctorQueryPort.getDoctor(doctorId);

        List<WorkingPatternResponse> patterns = doctorWorkingPatternRepository
                .findByDoctorIdAndDeletedAtIsNull(doctorId).stream().map(scheduleMapper::toResponse).toList();

        List<TimeOffResponse> timeOffs = doctorTimeOffRepository.findByDoctorIdAndDeletedAtIsNull(doctorId).stream()
                .map(scheduleMapper::toResponse).toList();

        return DoctorScheduleResponse.builder().doctorId(doctorId).workingPartterns(patterns).timeOffs(timeOffs)
                .build();
    }

    private void ensureScheduleAccess(Long doctorId) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> RoleConstants.ADMIN.equals(authority.getAuthority().replace("ROLE_", "")));

        if (isAdmin) {
            return;
        }

        Doctor doctor = doctorQueryPort.getDoctor(doctorId);
        if (doctor.getUser() == null || !currentUserId.equals(doctor.getUser().getId())) {
            throw new ForbiddenException("Access denied");
        }
    }

    private void ensureScheduleAccess(DoctorWorkingPattern pattern) {
        ensureScheduleAccess(pattern.getDoctor().getId());
    }

    private void ensureScheduleAccess(DoctorTimeOff timeOff) {
        ensureScheduleAccess(timeOff.getDoctor().getId());
    }

    private void clearSlotCacheSafely(String action) {
        try {
            scheduleCacheService.clearAllSlots();
        } catch (Exception e) {
            log.warn("Failed to clear cache after {}: {}", action, e.getMessage());
        }
    }

}