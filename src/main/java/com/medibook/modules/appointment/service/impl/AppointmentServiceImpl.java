package com.medibook.modules.appointment.service.impl;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.medibook.common.enums.AppointmentStatus;
import com.medibook.common.exception.BadRequestException;
import com.medibook.common.exception.ResourceNotFoundException;
import com.medibook.common.response.PageResponse;
import com.medibook.common.response.util.PageMapper;
import com.medibook.modules.appointment.dto.request.AppointmentCreateRequest;
import com.medibook.modules.appointment.dto.request.AppointmentRescheduleRequest;
import com.medibook.modules.appointment.dto.response.AppointmentResponse;
import com.medibook.modules.appointment.dto.response.BookedSlotResponse;
import com.medibook.modules.appointment.entity.Appointment;
import com.medibook.modules.appointment.entity.AppointmentStatusHistory;
import com.medibook.modules.appointment.mapper.AppointmentMapper;
import com.medibook.modules.appointment.repository.AppointmentRepository;
import com.medibook.modules.appointment.repository.AppointmentStatusHistoryRepository;
import com.medibook.modules.appointment.service.AppointmentService;
import com.medibook.modules.appointment.specification.AppointmentSpecification;
import com.medibook.modules.appointment.validator.AppointmentValidator;
import com.medibook.modules.doctor.entity.Doctor;
import com.medibook.modules.doctor.facade.DoctorFacade;
import com.medibook.modules.notification.service.EmailService;
import com.medibook.modules.schedule.entity.DoctorWorkingPattern;
import com.medibook.modules.schedule.facade.ScheduleFacade;
import com.medibook.modules.user.entity.User;
import com.medibook.modules.user.facade.UserFacade;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentStatusHistoryRepository historyRepository;

    private final DoctorFacade doctorFacade;
    private final UserFacade userFacade;
    private final ScheduleFacade scheduleFacade;
    private final AppointmentValidator validator;

    private final AppointmentMapper mapper;
    private final EmailService emailService;

    @Override
    public AppointmentResponse createAppointment(AppointmentCreateRequest request) {

        validator.validateBookingTime(request.getStartDateTime());

        Doctor doctor = doctorFacade.getDoctorEntityById(request.getDoctorId());

        if (doctor == null) {
            throw new BadRequestException("Doctor not found");
        }

        if (doctor.getDeletedAt() != null) {
            throw new BadRequestException("Doctor is deleted");
        }

        if (doctor.getUser() == null || !doctor.getUser().getIsActive()) {
            throw new BadRequestException("Doctor is inactive");
        }

        User patient = userFacade.getCurrentUserEntity();

        if (patient == null || !patient.getIsActive()) {
            throw new BadRequestException("Account is inactive");
        }

        LocalDateTime startTime = request.getStartDateTime();

        DoctorWorkingPattern pattern = scheduleFacade.getWorkingPattern(doctor.getId(), startTime);

        LocalDateTime endTime = startTime.plusMinutes(pattern.getSlotDuration());

        boolean overlap = appointmentRepository.existsPatientOverlap(patient.getId(), null, startTime, endTime);

        validator.validatePatientOverlap(overlap);

        appointmentRepository.findLockedAppointment(doctor.getId(), startTime);

        if (appointmentRepository.existsActiveAppointment(doctor.getId(), startTime)) {
            throw new BadRequestException("Slot already booked");
        }

        Appointment appointment = new Appointment();

        appointment.setBookingCode(generateBookingCode());
        appointment.setDoctor(doctor);
        appointment.setPatient(patient);
        appointment.setStartDatetime(startTime);
        appointment.setEndDatetime(startTime.plusMinutes(pattern.getSlotDuration()));
        appointment.setNote(request.getNote());
        appointment.setStatus(AppointmentStatus.PENDING);
        appointment.setConsultationFee(doctor.getConsultationFee());

        try {
            appointment = appointmentRepository.save(appointment);
        } catch (DataIntegrityViolationException ex) {
            throw new BadRequestException("Slot already booked");
        }

        saveHistory(appointment, null, AppointmentStatus.PENDING, patient);

        emailService.sendAppointmentCreatedEmail(appointment);

        return mapper.toResponse(appointment);
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentResponse getAppointment(Long id) {

        Appointment appointment = appointmentRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        User currentUser = userFacade.getCurrentUserEntity();

        if (!appointment.getPatient().getId().equals(currentUser.getId())) {
            throw new BadRequestException(
                    "You do not have permission to access this appointment");
        }

        return mapper.toResponse(appointment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AppointmentResponse> getMyAppointments(Pageable pageable) {

        User patient = userFacade.getCurrentUserEntity();

        return appointmentRepository.findByPatientIdAndDeletedAtIsNull(patient.getId(), pageable)
                .map(mapper::toResponse);
    }

    @Override
    public void cancelAppointment(Long id, String reason) {

        Appointment appointment = appointmentRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new BadRequestException(
                    "Appointment already cancelled");
        }

        if (appointment.getStatus() != AppointmentStatus.PENDING
                && appointment.getStatus() != AppointmentStatus.CONFIRMED) {

            throw new BadRequestException(
                    "Appointment cannot be cancelled");
        }

        if (appointment.getStartDatetime()
                .isBefore(LocalDateTime.now().plusHours(24))) {

            throw new BadRequestException(
                    "Appointment can only be cancelled at least 24 hours in advance");
        }

        User user = userFacade.getCurrentUserEntity();

        if (!appointment.getPatient().getId().equals(user.getId())) {
            throw new BadRequestException(
                    "You do not have permission to access this appointment");
        }

        AppointmentStatus oldStatus = appointment.getStatus();

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setCancelledReason(reason);

        appointmentRepository.save(appointment);

        saveHistory(appointment, oldStatus, AppointmentStatus.CANCELLED, user);

        emailService.sendAppointmentCancelledEmail(appointment);
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

        Appointment appointment = appointmentRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        AppointmentStatus current = appointment.getStatus();

        boolean valid = current == AppointmentStatus.PENDING;

        if (!valid) {
            throw new BadRequestException("Invalid appointment status transition");
        }

        AppointmentStatus oldStatus = appointment.getStatus();

        appointment.setStatus(AppointmentStatus.CONFIRMED);

        appointmentRepository.save(appointment);

        saveHistory(appointment, oldStatus, AppointmentStatus.CONFIRMED, userFacade.getCurrentUserEntity());
    }

    @Override
    public void completeAppointment(Long id) {

        Appointment appointment = appointmentRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new BadRequestException(
                    "Invalid appointment status transition");
        }

        AppointmentStatus oldStatus = appointment.getStatus();

        appointment.setStatus(AppointmentStatus.COMPLETED);

        appointmentRepository.save(appointment);

        saveHistory(appointment, oldStatus, AppointmentStatus.COMPLETED, userFacade.getCurrentUserEntity());
    }

    @Override
    public void markNoShow(Long id) {

        Appointment appointment = appointmentRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new BadRequestException("Invalid appointment status transition");
        }

        AppointmentStatus oldStatus = appointment.getStatus();

        appointment.setStatus(AppointmentStatus.NO_SHOW);

        appointmentRepository.save(appointment);

        saveHistory(appointment, oldStatus, AppointmentStatus.NO_SHOW, userFacade.getCurrentUserEntity());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AppointmentResponse> getDoctorAppointments(AppointmentStatus status, LocalDate from, LocalDate to,
            Pageable pageable) {

        User currentUser = userFacade.getCurrentUserEntity();

        Doctor doctor = doctorFacade.getDoctorByUserId(currentUser.getId());

        return appointmentRepository
                .findDoctorAppointments(doctor.getId(), status, from.atStartOfDay(), to.plusDays(1).atStartOfDay(),
                        pageable)
                .map(mapper::toResponse);
    }

    @Override
    public AppointmentResponse rescheduleAppointment(Long appointmentId, AppointmentRescheduleRequest request) {

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        User patient = userFacade.getCurrentUserEntity();

        if (!appointment.getPatient().getId().equals(patient.getId())) {
            throw new BadRequestException(
                    "You do not have permission to access this appointment");
        }

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new BadRequestException("Appointment already cancelled");
        }

        if (appointment.getStatus() != AppointmentStatus.PENDING
                && appointment.getStatus() != AppointmentStatus.CONFIRMED) {

            throw new BadRequestException("Appointment cannot be cancelled");
        }

        if (appointment.getStartDatetime().isBefore(LocalDateTime.now().plusHours(24))) {

            throw new BadRequestException("Appointment can only be cancelled at least 24 hours in advance");
        }

        LocalDateTime newStart = request.getNewStartDatetime();

        validator.validateBookingTime(newStart);

        DoctorWorkingPattern pattern = scheduleFacade.getWorkingPattern(appointment.getDoctor().getId(),
                newStart);

        LocalDateTime newEnd = newStart.plusMinutes(pattern.getSlotDuration());

        boolean overlap = appointmentRepository.existsPatientOverlap(patient.getId(), appointment.getId(), newStart,
                newEnd);

        validator.validatePatientOverlap(overlap);

        if (appointmentRepository.existsActiveAppointment(appointment.getDoctor().getId(), newStart)) {

            throw new BadRequestException("Slot already booked");
        }

        appointment.setStartDatetime(newStart);
        appointment.setEndDatetime(newEnd);

        appointmentRepository.save(appointment);

        return mapper.toResponse(appointment);
    }

    @Override
    public Appointment getAppointmentEntity(Long id) {
        return appointmentRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));
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
}