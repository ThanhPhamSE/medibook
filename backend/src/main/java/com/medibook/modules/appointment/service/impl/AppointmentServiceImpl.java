package com.medibook.modules.appointment.service.impl;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.medibook.common.constant.RoleConstants;
import com.medibook.common.enums.AppointmentStatus;
import com.medibook.common.exception.BadRequestException;
import com.medibook.common.exception.ForbiddenException;
import com.medibook.common.exception.ResourceNotFoundException;
import com.medibook.common.response.PageResponse;
import com.medibook.common.response.util.PageMapper;
import com.medibook.modules.appointment.dto.request.AppointmentCreateRequest;
import com.medibook.modules.appointment.dto.request.AppointmentRescheduleRequest;
import com.medibook.modules.appointment.dto.response.AppointmentResponse;
import com.medibook.modules.appointment.dto.response.AppointmentStatsResponse;
import com.medibook.modules.appointment.dto.response.BookedSlotResponse;

import com.medibook.modules.appointment.entity.Appointment;
import com.medibook.modules.appointment.entity.AppointmentStatusHistory;
import com.medibook.modules.appointment.mapper.AppointmentMapper;
import com.medibook.modules.appointment.repository.AppointmentRepository;
import com.medibook.modules.appointment.repository.StatusCountProjection;
import com.medibook.modules.appointment.repository.AppointmentStatusHistoryRepository;

import com.medibook.modules.appointment.service.AppointmentService;
import com.medibook.modules.appointment.service.RedisLockService;
import com.medibook.modules.appointment.specification.AppointmentSpecification;
import com.medibook.security.util.SecurityUtils;
import com.medibook.modules.appointment.policy.AppointmentStatusPolicy;
import com.medibook.modules.appointment.validator.AppointmentValidator;
import com.medibook.modules.doctor.entity.Doctor;
import com.medibook.modules.doctor.facade.DoctorFacade;
import com.medibook.modules.notification.dto.AppointmentEmailData;
import com.medibook.modules.notification.service.EmailService;
import com.medibook.modules.schedule.entity.DoctorWorkingPattern;
import com.medibook.modules.schedule.facade.ScheduleFacade;
import com.medibook.modules.user.entity.User;
import com.medibook.modules.user.facade.UserFacade;
import com.medibook.modules.audit.service.AuditService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentStatusHistoryRepository historyRepository;

    private final DoctorFacade doctorFacade;
    private final UserFacade userFacade;
    private final ScheduleFacade scheduleFacade;
    private final AppointmentValidator validator;
    private final AppointmentStatusPolicy statusPolicy;

    private final AppointmentMapper mapper;
    private final EmailService emailService;
    private final AuditService auditService;
    private final JdbcTemplate jdbcTemplate;
    private final RedisLockService redisLockService;

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    @Override
    public AppointmentResponse createAppointment(AppointmentCreateRequest request) {
        log.info("Request to create appointment: doctorId={}, startTime={}", request.getDoctorId(),
                request.getStartDateTime());

        validator.validateBookingTime(request.getStartDateTime());

        Doctor doctor = doctorFacade.getDoctorForBooking(request.getDoctorId());

        if (doctor == null) {
            throw new BadRequestException("Doctor not found");
        }

        if (doctor.getDeletedAt() != null) {
            throw new BadRequestException("Doctor is deleted");
        }

        if (doctor.getUser() == null || !doctor.getUser().getIsActive()) {
            throw new BadRequestException("Doctor is inactive");
        }

        if (doctor.getSpecialty() != null && doctor.getSpecialty().getDeletedAt() != null) {
            throw new BadRequestException("Doctor's specialty is no longer active");
        }

        if (doctor.getConsultationFee() == null
                || doctor.getConsultationFee().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Invalid consultation fee");
        }
                                                                                                                                                                         
        User patient = userFacade.getCurrentUserEntity();

        if (doctor.getUser() != null && doctor.getUser().getId().equals(patient.getId())) {
            throw new BadRequestException("Doctors cannot book appointments with themselves");
        }

        if (patient == null || !patient.getIsActive()) {
            throw new BadRequestException("Account is inactive");
        }
                                                                                            
        LocalDateTime startTime = request.getStartDateTime();

        DoctorWorkingPattern pattern = scheduleFacade.getWorkingPattern(doctor.getId(), startTime);

        LocalDateTime endTime = startTime.plusMinutes(pattern.getSlotDuration());

        if (scheduleFacade.isDoctorOnTimeOff(doctor.getId(), startTime, endTime)) {
            throw new BadRequestException("Doctor is on time off during this period");
        }

        boolean overlap = appointmentRepository.existsPatientOverlap(patient.getId(), null, startTime, endTime);
                                                                                                                                                              
        validator.validatePatientOverlap(overlap);

        String slotKey = startTime.toString();
        String lockKey = "appointment_slot_" + doctor.getId() + "_" + slotKey;
        boolean lockAcquired = redisLockService.acquireLock(lockKey, 10);

        if (!lockAcquired) {
            throw new BadRequestException("Could not acquire slot lock, please try again");
        }

        try {
            appointmentRepository.findLockedAppointment(doctor.getId(), startTime).ifPresent(a -> {
                throw new BadRequestException("Slot already booked");
            });

            Appointment appointment = new Appointment();

            appointment.setBookingCode(generateBookingCode());
            appointment.setDoctor(doctor);
            appointment.setPatient(patient);
            appointment.setStartDatetime(startTime);
            appointment.setEndDatetime(startTime.plusMinutes(pattern.getSlotDuration()));
            appointment.setNote(request.getNote());
            appointment.setStatus(AppointmentStatus.PENDING);
            appointment.setConsultationFee(doctor.getConsultationFee());

            Appointment savedAppointment;
            try {
                savedAppointment = appointmentRepository.save(appointment);
                // Flush to DB so unique constraint violations trigger
                // DataIntegrityViolationException immediately within the try-catch block
                appointmentRepository.flush();
            } catch (DataIntegrityViolationException e) {
                throw new BadRequestException("Slot already booked");
            }

            saveHistory(savedAppointment, null, AppointmentStatus.PENDING, patient);

            try {
                scheduleFacade.evictSlots(doctor.getId(), startTime.toLocalDate());
            } catch (Exception e) {
                log.warn("Failed to evict cache after creating appointment: {}", e.getMessage());
            }

            auditService.log("CREATE", "Appointment", savedAppointment.getId(), null, savedAppointment);

            sendEmailAfterCommit(
                    () -> emailService.sendAppointmentCreatedEmail(AppointmentEmailData.from(savedAppointment)));

            log.info("Successfully created appointment: id={}, doctorId={}, patientId={}, startTime={}",
                    savedAppointment.getId(), doctor.getId(), patient.getId(), startTime);

            return mapper.toResponse(savedAppointment);
        } finally {
            redisLockService.releaseLock(lockKey);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentResponse getAppointment(Long id) {

        Appointment appointment = appointmentRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        ensureAppointmentAccess(appointment);

        return mapper.toResponse(appointment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AppointmentResponse> getMyAppointments(AppointmentStatus status, String timeFilter, Pageable pageable) {

        Long currentUserId = SecurityUtils.getCurrentUserId();
        String roleName = SecurityUtils.getCurrentRoleName();
        LocalDateTime now = LocalDateTime.now();

        if (RoleConstants.DOCTOR.equals(roleName)) {
            Doctor doctor = doctorFacade.getDoctorByUserId(currentUserId);

            var spec = AppointmentSpecification.notDeleted()
                    .and(AppointmentSpecification.hasDoctorId(doctor.getId()))
                    .and(AppointmentSpecification.hasStatus(status))
                    .and(buildTimeFilterSpec(timeFilter, now));

            return appointmentRepository.findAll(spec, pageable).map(mapper::toResponse);
        }

        var spec = AppointmentSpecification.notDeleted()
                .and(AppointmentSpecification.hasPatientId(currentUserId))
                .and(AppointmentSpecification.hasStatus(status))
                .and(buildTimeFilterSpec(timeFilter, now));

        return appointmentRepository.findAll(spec, pageable).map(mapper::toResponse);
    }

    private Specification<Appointment> buildTimeFilterSpec(
            String timeFilter,
            LocalDateTime now) {

        if ("upcoming".equalsIgnoreCase(timeFilter)) {
            return AppointmentSpecification.startAfter(now);
        } else if ("past".equalsIgnoreCase(timeFilter)) {
            return AppointmentSpecification.startBefore(now);
        }

        return (root, query, cb) -> null;
    }

    @Override
    public void cancelAppointment(Long id, String reason) {
        log.info("Request to cancel appointment: id={}", id);

        Appointment appointment = appointmentRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        statusPolicy.validateCancelTransition(appointment.getStatus());

        if (reason == null || reason.trim().isEmpty()) {
            throw new BadRequestException("Cancellation reason is required");
        }

        if (appointment.getStartDatetime()
                .isBefore(LocalDateTime.now().plusHours(24))) {

            throw new BadRequestException(
                    "Appointment can only be cancelled at least 24 hours in advance");
        }

        User user = userFacade.getCurrentUserEntity();

        ensurePatientAccess(appointment, user);

        AppointmentStatus oldStatus = appointment.getStatus();

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setCancelledBy(user);
        appointment.setCancelledReason(reason);

        appointmentRepository.save(appointment);

        saveHistory(appointment, oldStatus, AppointmentStatus.CANCELLED, user);

        try {
            scheduleFacade.evictSlots(appointment.getDoctor().getId(),
                    appointment.getStartDatetime().toLocalDate());
        } catch (Exception e) {
            log.warn("Failed to evict cache after cancelling appointment: {}", e.getMessage());
        }

        auditService.log("CANCEL", "Appointment", appointment.getId(), oldStatus, AppointmentStatus.CANCELLED);

        sendEmailAfterCommit(() -> emailService.sendAppointmentCancelledEmail(AppointmentEmailData.from(appointment)));

        log.info("Successfully cancelled appointment: id={}, patientId={}", id, user.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isSlotBooked(Long doctorId, LocalDateTime startDatetime) {
        return appointmentRepository.existsActiveAppointment(doctorId, startDatetime);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookedSlotResponse> getBookedAppointmentsByDate(Long doctorId, LocalDate date) {
        return appointmentRepository.findActiveAppointmentsByDate(
                doctorId,
                date.atStartOfDay(),
                date.plusDays(1).atStartOfDay()).stream().map(mapper::toBookedSlot).toList();
    }

    @Override
    public void confirmAppointment(Long id) {
        log.info("Request to confirm appointment: id={}", id);

        Appointment appointment = appointmentRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        if (appointment.getStartDatetime().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Cannot confirm appointment that has already started or passed");
        }

        if (appointment.getDoctor().getDeletedAt() != null) {
            throw new BadRequestException("Cannot confirm appointment for deleted doctor");
        }

        if (appointment.getDoctor().getUser() == null || !appointment.getDoctor().getUser().getIsActive()) {
            throw new BadRequestException("Cannot confirm appointment for inactive doctor");
        }

        User currentUser = userFacade.getCurrentUserEntity();
        ensureDoctorOrAdminAccess(appointment, currentUser);

        statusPolicy.validateConfirmTransition(appointment.getStatus());

        AppointmentStatus oldStatus = appointment.getStatus();

        appointment.setStatus(AppointmentStatus.CONFIRMED);

        appointmentRepository.save(appointment);

        saveHistory(appointment, oldStatus, AppointmentStatus.CONFIRMED, currentUser);

        auditService.log("CONFIRM", "Appointment", appointment.getId(), oldStatus, AppointmentStatus.CONFIRMED);

        sendEmailAfterCommit(
                () -> emailService.sendAppointmentCreatedEmail(AppointmentEmailData.from(appointment)));

        log.info("Successfully confirmed appointment: id={}", id);
    }

    @Override
    public void confirmAppointmentPaid(Long id) {
        log.info("System request to confirm paid appointment: id={}", id);

        Appointment appointment = appointmentRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        if (appointment.getDoctor().getDeletedAt() != null) {
            throw new BadRequestException("Cannot confirm appointment for deleted doctor");
        }

        if (appointment.getDoctor().getUser() == null || !appointment.getDoctor().getUser().getIsActive()) {
            throw new BadRequestException("Cannot confirm appointment for inactive doctor");
        }

        statusPolicy.validateConfirmTransition(appointment.getStatus());

        AppointmentStatus oldStatus = appointment.getStatus();

        appointment.setStatus(AppointmentStatus.CONFIRMED);

        appointmentRepository.save(appointment);

        // Không gọi saveHistory ở đây vì đây là system action (PayOS webhook/verify)
        // không có user đang đăng nhập — changed_by sẽ null gây lỗi DB constraint.
        // Audit trail vẫn được lưu qua auditService.log bên dưới.
        auditService.log("CONFIRM_PAID", "Appointment", appointment.getId(), oldStatus, AppointmentStatus.CONFIRMED);

        sendEmailAfterCommit(
                () -> emailService.sendAppointmentCreatedEmail(AppointmentEmailData.from(appointment)));

        log.info("Successfully confirmed paid appointment: id={}", id);
    }

    @Override
    public void completeAppointment(Long id) {
        log.info("Request to complete appointment: id={}", id);

        Appointment appointment = appointmentRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        if (appointment.getStartDatetime().isAfter(LocalDateTime.now())) {
            throw new BadRequestException("Cannot complete appointment that has not started yet");
        }

        if (appointment.getDoctor().getDeletedAt() != null) {
            throw new BadRequestException("Cannot complete appointment for deleted doctor");
        }

        if (appointment.getDoctor().getUser() == null || !appointment.getDoctor().getUser().getIsActive()) {
            throw new BadRequestException("Cannot complete appointment for inactive doctor");
        }

        User currentUser = userFacade.getCurrentUserEntity();
        ensureDoctorOrAdminAccess(appointment, currentUser);

        statusPolicy.validateCompleteTransition(appointment.getStatus());

        AppointmentStatus oldStatus = appointment.getStatus();

        appointment.setStatus(AppointmentStatus.COMPLETED);

        appointmentRepository.save(appointment);

        saveHistory(appointment, oldStatus, AppointmentStatus.COMPLETED, currentUser);

        auditService.log("COMPLETE", "Appointment", appointment.getId(), oldStatus, AppointmentStatus.COMPLETED);

        sendEmailAfterCommit(
                () -> emailService.sendAppointmentCreatedEmail(AppointmentEmailData.from(appointment)));

        log.info("Successfully completed appointment: id={}", id);
    }

    @Override
    public void markNoShow(Long id) {
        log.info("Request to mark appointment as no-show: id={}", id);

        Appointment appointment = appointmentRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        if (appointment.getStartDatetime().isAfter(LocalDateTime.now())) {
            throw new BadRequestException("Cannot mark appointment as no-show before the appointment time");
        }

        User currentUser = userFacade.getCurrentUserEntity();
        ensureDoctorOrAdminAccess(appointment, currentUser);

        statusPolicy.validateNoShowTransition(appointment.getStatus());

        AppointmentStatus oldStatus = appointment.getStatus();

        appointment.setStatus(AppointmentStatus.NO_SHOW);

        appointmentRepository.save(appointment);

        saveHistory(appointment, oldStatus, AppointmentStatus.NO_SHOW, currentUser);

        auditService.log("NO_SHOW", "Appointment", appointment.getId(), oldStatus, AppointmentStatus.NO_SHOW);

        log.info("Successfully marked appointment as no-show: id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AppointmentResponse> getDoctorAppointments(AppointmentStatus status, LocalDate from, LocalDate to,
            Pageable pageable) {

        User currentUser = userFacade.getCurrentUserEntity();
        ensureDoctorOrAdminAccessForDoctorScope(currentUser);

        Doctor doctor = doctorFacade.getDoctorByUserId(currentUser.getId());

        return appointmentRepository
                .findDoctorAppointments(doctor.getId(), status, from.atStartOfDay(), to.plusDays(1).atStartOfDay(),
                        pageable)
                .map(mapper::toResponse);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    @Override
    public AppointmentResponse rescheduleAppointment(Long appointmentId, AppointmentRescheduleRequest request) {
        log.info("Request to reschedule appointment: id={}, newStartTime={}", appointmentId,
                request.getNewStartDatetime());

        Appointment appointment = appointmentRepository.findByIdAndDeletedAtIsNull(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        User patient = userFacade.getCurrentUserEntity();

        ensurePatientAccess(appointment, patient);

        statusPolicy.validateRescheduleTransition(appointment.getStatus());

        if (appointment.getStartDatetime().isBefore(LocalDateTime.now().plusHours(24))) {

            throw new BadRequestException("Appointment can only be rescheduled at least 24 hours in advance.");
        }

        LocalDateTime newStart = request.getNewStartDatetime();

        validator.validateBookingTime(newStart);

        DoctorWorkingPattern pattern = scheduleFacade.getWorkingPattern(appointment.getDoctor().getId(),
                newStart);

        LocalDateTime newEnd = newStart.plusMinutes(pattern.getSlotDuration());

        if (scheduleFacade.isDoctorOnTimeOff(appointment.getDoctor().getId(), newStart, newEnd)) {
            throw new BadRequestException("Doctor is on time off during this period");
        }

        boolean overlap = appointmentRepository.existsPatientOverlap(patient.getId(), appointment.getId(), newStart,
                newEnd);

        validator.validatePatientOverlap(overlap);

        if (appointment.getStartDatetime().equals(newStart)) {
            throw new BadRequestException(
                    "The new appointment time must be different from the current appointment time.");
        }

        String newSlotKey = newStart.toString();
        String lockKey = "appointment_slot_" + appointment.getDoctor().getId() + "_" + newSlotKey;
        boolean lockAcquired = redisLockService.acquireLock(lockKey, 10);

        if (!lockAcquired) {
            throw new BadRequestException("Could not acquire slot lock, please try again");
        }

        try {
            appointmentRepository.findLockedAppointment(appointment.getDoctor().getId(), newStart).ifPresent(a -> {
                if (!a.getId().equals(appointment.getId())) {
                    throw new BadRequestException("Slot already booked");
                }
            });

            LocalDate oldDate = appointment.getStartDatetime().toLocalDate();

            appointment.setStartDatetime(newStart);
            appointment.setEndDatetime(newEnd);

            appointmentRepository.save(appointment);

            saveHistory(appointment, appointment.getStatus(), appointment.getStatus(), patient);

            auditService.log("RESCHEDULE", "Appointment", appointment.getId(), oldDate,
                    appointment.getStartDatetime().toLocalDate());

            try {
                scheduleFacade.evictSlots(appointment.getDoctor().getId(), oldDate);
                scheduleFacade.evictSlots(appointment.getDoctor().getId(),
                        appointment.getStartDatetime().toLocalDate());
            } catch (Exception e) {
                log.warn("Failed to evict cache after rescheduling appointment: {}", e.getMessage());
            }

            sendEmailAfterCommit(
                    () -> emailService.sendAppointmentCreatedEmail(AppointmentEmailData.from(appointment)));

            log.info("Successfully rescheduled appointment: id={}, oldDate={}, newDate={}", appointmentId, oldDate,
                    appointment.getStartDatetime().toLocalDate());

            return mapper.toResponse(appointment);
        } finally {
            redisLockService.releaseLock(lockKey);
        }
    }

    @Override
    public Appointment getAppointmentEntity(Long id) {
        return appointmentRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));
    }

    private void ensureAppointmentAccess(Appointment appointment) {
        User currentUser = userFacade.getCurrentUserEntity();
        if (currentUser == null) {
            throw new ForbiddenException("Access denied");
        }

        if (isAdmin(currentUser)) {
            return;
        }

        if (appointment.getDoctor() != null && appointment.getDoctor().getUser() != null
                && currentUser.getId().equals(appointment.getDoctor().getUser().getId())) {
            return;
        }

        if (appointment.getPatient() != null && currentUser.getId().equals(appointment.getPatient().getId())) {
            return;
        }

        throw new ForbiddenException("Access denied");
    }

    private void ensurePatientAccess(Appointment appointment, User currentUser) {
        if (currentUser == null) {
            throw new ForbiddenException("Access denied");
        }

        if (isAdmin(currentUser)) {
            return;
        }

        if (appointment.getPatient() != null && currentUser.getId().equals(appointment.getPatient().getId())) {
            return;
        }

        throw new ForbiddenException("Access denied");
    }

    private void ensureDoctorOrAdminAccess(Appointment appointment, User currentUser) {
        if (currentUser == null) {
            throw new ForbiddenException("Access denied");
        }

        if (isAdmin(currentUser)) {
            return;
        }

        if (appointment.getDoctor() != null && appointment.getDoctor().getUser() != null
                && currentUser.getId().equals(appointment.getDoctor().getUser().getId())) {
            return;
        }

        throw new ForbiddenException("Access denied");
    }

    private void ensureDoctorOrAdminAccessForDoctorScope(User currentUser) {
        if (currentUser == null) {
            throw new ForbiddenException("Access denied");
        }

        if (isAdmin(currentUser)) {
            return;
        }

        if (currentUser.getRole() != null && RoleConstants.DOCTOR.equals(currentUser.getRole().getName())) {
            return;
        }

        throw new ForbiddenException("Access denied");
    }

    private boolean isAdmin(User user) {
        return user != null && user.getRole() != null
                && RoleConstants.ADMIN.equals(user.getRole().getName());
    }

    private void sendEmailAfterCommit(Runnable action) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
        } else {
            action.run();
        }
    }

    private String generateBookingCode() {

        String code;

        do {
            code = java.util.UUID.randomUUID().toString().substring(0, 10).toUpperCase();
        } while (appointmentRepository.existsByBookingCode(code));

        return code;
    }

    private void saveHistory(Appointment appointment, AppointmentStatus oldStatus, AppointmentStatus newStatus,
            User changedBy) {

        AppointmentStatusHistory history = new AppointmentStatusHistory();

        history.setAppointment(appointment);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setChangedBy(changedBy);
        history.setChangedAt(LocalDateTime.now());

        historyRepository.save(history);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AppointmentResponse> getAllBookings(Pageable pageable) {

        var spec = AppointmentSpecification.notDeleted();

        return PageMapper.from(appointmentRepository.findAll(spec, pageable), mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AppointmentResponse> getMonthlySchedule(Long doctorId, AppointmentStatus status, LocalDate from,
            LocalDate to, Pageable pageable) {

        var spec = AppointmentSpecification.notDeleted()
                .and(AppointmentSpecification.hasDoctorId(doctorId))
                .and(AppointmentSpecification.hasStatus(status))
                .and(AppointmentSpecification.startAfter(from != null ? from.atStartOfDay() : null))
                .and(AppointmentSpecification.startBefore(to != null ? to.plusDays(1).atStartOfDay() : null));

        return PageMapper.from(appointmentRepository.findAll(spec, pageable), mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AppointmentResponse> searchAdminBookings(String bookingCode, Long doctorId, Long patientId,
            AppointmentStatus status, LocalDate from, LocalDate to, Pageable pageable) {

        var spec = AppointmentSpecification.notDeleted()
                .and(AppointmentSpecification.hasBookingCodeLike(bookingCode))
                .and(AppointmentSpecification.hasDoctorId(doctorId))
                .and(AppointmentSpecification.hasPatientId(patientId))
                .and(AppointmentSpecification.hasStatus(status))
                .and(AppointmentSpecification.startAfter(from != null ? from.atStartOfDay() : null))
                .and(AppointmentSpecification.startBefore(to != null ? to.plusDays(1).atStartOfDay() : null));

        return PageMapper.from(appointmentRepository.findAll(spec, pageable), mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AppointmentResponse> getTodayAppointments(Pageable pageable) {

        LocalDate today = LocalDate.now();

        return getDoctorAppointments(null, today, today, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AppointmentResponse> getCurrentWeekAppointments(Pageable pageable) {

        LocalDate today = LocalDate.now();

        LocalDate start = today.with(DayOfWeek.MONDAY);

        LocalDate end = today.with(DayOfWeek.SUNDAY);

        return getDoctorAppointments(null, start, end, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getUpcomingAppointments() {

        User currentUser = userFacade.getCurrentUserEntity();

        Pageable pageable = PageRequest.of(0, 5, Sort.by("startDatetime").ascending());

        var spec = AppointmentSpecification.notDeleted().and(AppointmentSpecification.hasPatientId(currentUser.getId()))
                .and(AppointmentSpecification.isUpcoming());

        return appointmentRepository.findAll(spec, pageable).stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentStatsResponse getMyAppointmentsStats() {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        String roleName = SecurityUtils.getCurrentRoleName();
        List<StatusCountProjection> projections;

        if (RoleConstants.DOCTOR.equals(roleName)) {
            Doctor doctor = doctorFacade.getDoctorByUserId(currentUserId);
            projections = appointmentRepository.countDoctorGroupByStatus(doctor.getId());
        } else {
            projections = appointmentRepository.countPatientGroupByStatus(currentUserId);
        }

        long total = 0;
        long completed = 0;
        long pending = 0;
        long cancelled = 0;

        for (StatusCountProjection p : projections) {
            long cnt = p.getCnt() != null ? p.getCnt() : 0;
            total += cnt;
            if (p.getStatus() == AppointmentStatus.COMPLETED) {
                completed = cnt;
            } else if (p.getStatus() == AppointmentStatus.PENDING) {
                pending = cnt;
            } else if (p.getStatus() == AppointmentStatus.CANCELLED) {
                cancelled = cnt;
            }
        }

        return AppointmentStatsResponse.builder()
                .total(total)
                .completed(completed)
                .pending(pending)
                .cancelled(cancelled)
                .build();
    }

}
