package com.medibook.modules.appointment.service.serviceImpl;

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
import com.medibook.modules.appointment.dto.request.AppointmentCreateRequest;
import com.medibook.modules.appointment.dto.request.AppointmentRescheduleRequest;
import com.medibook.modules.appointment.dto.response.AppointmentResponse;
import com.medibook.modules.appointment.entity.Appointment;
import com.medibook.modules.appointment.entity.AppointmentStatusHistory;
import com.medibook.modules.appointment.mapper.AppointmentMapper;
import com.medibook.modules.appointment.repository.AppointmentRepository;
import com.medibook.modules.appointment.repository.AppointmentStatusHistoryRepository;
import com.medibook.modules.appointment.service.AppointmentService;
import com.medibook.modules.appointment.validator.AppointmentValidator;
import com.medibook.modules.doctor.entity.Doctor;
import com.medibook.modules.doctor.service.DoctorService;
import com.medibook.modules.schedule.entity.DoctorWorkingPattern;
import com.medibook.modules.schedule.validator.ScheduleValidator;
import com.medibook.modules.user.entity.User;
import com.medibook.modules.user.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentStatusHistoryRepository historyRepository;

    private final DoctorService doctorService;
    private final UserService userService;
    private final ScheduleValidator scheduleValidator;
    private final AppointmentValidator validator;

    private final AppointmentMapper mapper;

    @Override
    public AppointmentResponse createAppointment(AppointmentCreateRequest request) {

        validator.validateBookingTime(request.getStartDateTime());

        Doctor doctor = doctorService.getDoctorEntityById(request.getDoctorId());
        validator.validateDoctor(doctor);

        User patient = userService.getCurrentUser();
        validator.validatePatient(patient);

        LocalDateTime startTime = request.getStartDateTime();

        DoctorWorkingPattern pattern = scheduleValidator.validateDoctorWorkingPattern(doctor.getId(), startTime);

        LocalDateTime endTime = startTime.plusMinutes(pattern.getSlotDuration());

        validator.validatePatientOverlap(patient.getId(), null, startTime, endTime);

        appointmentRepository.findLockedAppointment(doctor.getId(), endTime);

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

        return mapper.toResponse(appointment);
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentResponse getAppointment(Long id) {

        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Appointment not found"));

        User currentUser = userService.getCurrentUser();

        validator.validateAppointmentOwner(appointment, currentUser);

        return mapper.toResponse(appointment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AppointmentResponse> getMyAppointments(Pageable pageable) {

        User patient = userService.getCurrentUser();

        return appointmentRepository.findByPatientIdAndDeletedAtIsNull(patient.getId(), pageable)
                .map(mapper::toResponse);
    }

    @Override
    public void cancelAppointment(Long id, String reason) {

        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Appointment not found"));

        validator.validateCancelable(appointment);

        User user = userService.getCurrentUser();

        validator.validateAppointmentOwner(appointment, user);

        AppointmentStatus oldStatus = appointment.getStatus();

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setNote(reason);

        appointmentRepository.save(appointment);

        saveHistory(appointment, oldStatus, AppointmentStatus.CANCELLED, user);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isSlotBooked(Long doctorId, LocalDateTime startDatetime) {
        return appointmentRepository.existsActiveAppointment(doctorId, startDatetime);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Appointment> getBookedAppointmentsByDate(Long doctorId, LocalDate date) {
        return appointmentRepository.findActiveAppointmentsByDate(
                doctorId,
                date.atStartOfDay(),
                date.plusDays(1).atStartOfDay());
    }

    @Override
    public void confirmAppointment(Long id) {

        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Appointment not found"));

        validator.validateTransition(appointment, AppointmentStatus.CONFIRMED);

        AppointmentStatus oldStatus = appointment.getStatus();

        appointment.setStatus(AppointmentStatus.CONFIRMED);

        appointmentRepository.save(appointment);

        saveHistory(appointment, oldStatus, AppointmentStatus.CONFIRMED, userService.getCurrentUser());
    }

    @Override
    public void completeAppointment(Long id) {

        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Appointment not found"));

        validator.validateTransition(appointment, AppointmentStatus.COMPLETED);

        AppointmentStatus oldStatus = appointment.getStatus();

        appointment.setStatus(AppointmentStatus.COMPLETED);

        appointmentRepository.save(appointment);

        saveHistory(appointment, oldStatus, AppointmentStatus.COMPLETED, userService.getCurrentUser());
    }

    @Override
    public void markNoShow(Long id) {

        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Appointment not found"));

        validator.validateTransition(appointment, AppointmentStatus.NO_SHOW);

        AppointmentStatus oldStatus = appointment.getStatus();

        appointment.setStatus(AppointmentStatus.NO_SHOW);

        appointmentRepository.save(appointment);

        saveHistory(appointment, oldStatus, AppointmentStatus.NO_SHOW, userService.getCurrentUser());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AppointmentResponse> getDoctorAppointments(AppointmentStatus status, LocalDate from, LocalDate to,
            Pageable pageable) {

        User currentUser = userService.getCurrentUser();

        Doctor doctor = doctorService.getDoctorByUserId(currentUser.getId());

        return appointmentRepository
                .findDoctorAppointments(doctor.getId(), status, from.atStartOfDay(), to.plusDays(1).atStartOfDay(),
                        pageable)
                .map(mapper::toResponse);
    }

    @Override
    public AppointmentResponse rescheduleAppointment(Long appointmentId, AppointmentRescheduleRequest request) {

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new BadRequestException("Appointment not found"));

        User patient = userService.getCurrentUser();

        validator.validateAppointmentOwner(appointment, patient);

        validator.validateCancelable(appointment);

        LocalDateTime newStart = request.getNewStartDatetime();

        validator.validateBookingTime(newStart);

        DoctorWorkingPattern pattern = scheduleValidator.validateDoctorWorkingPattern(appointment.getDoctor().getId(),
                newStart);

        LocalDateTime newEnd = newStart.plusMinutes(pattern.getSlotDuration());

        validator.validatePatientOverlap(patient.getId(), appointment.getId(), newStart, newEnd);

        if (appointmentRepository.existsActiveAppointment(appointment.getDoctor().getId(), newStart)) {

            throw new BadRequestException("Slot already booked");
        }

        appointment.setStartDatetime(newStart);
        appointment.setEndDatetime(newEnd);

        appointmentRepository.save(appointment);

        return mapper.toResponse(appointment);
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
}