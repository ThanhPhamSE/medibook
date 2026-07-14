package com.medibook.modules.appointment.service;

import com.medibook.common.enums.AppointmentStatus;
import com.medibook.common.exception.BadRequestException;
import com.medibook.common.exception.ForbiddenException;
import com.medibook.common.exception.ResourceNotFoundException;
import com.medibook.modules.appointment.dto.request.AppointmentCreateRequest;
import com.medibook.modules.appointment.dto.request.AppointmentRescheduleRequest;
import com.medibook.modules.appointment.dto.response.AppointmentResponse;
import com.medibook.modules.appointment.dto.response.AppointmentStatsResponse;
import com.medibook.modules.appointment.dto.response.BookedSlotResponse;
import com.medibook.modules.appointment.entity.Appointment;
import com.medibook.modules.appointment.mapper.AppointmentMapper;
import com.medibook.modules.appointment.policy.AppointmentStatusPolicy;
import com.medibook.modules.appointment.repository.AppointmentRepository;
import com.medibook.modules.appointment.repository.AppointmentStatusHistoryRepository;
import com.medibook.modules.appointment.service.impl.AppointmentServiceImpl;
import com.medibook.modules.appointment.specification.AppointmentSpecification;
import com.medibook.modules.appointment.validator.AppointmentValidator;
import com.medibook.modules.audit.service.AuditService;
import com.medibook.modules.doctor.entity.Doctor;
import com.medibook.modules.doctor.facade.DoctorFacade;
import com.medibook.modules.notification.dto.AppointmentEmailData;
import com.medibook.modules.notification.service.EmailService;
import com.medibook.modules.schedule.cache.ScheduleCacheService;
import com.medibook.modules.schedule.entity.DoctorWorkingPattern;
import com.medibook.modules.schedule.facade.ScheduleFacade;
import com.medibook.modules.user.entity.User;
import com.medibook.modules.user.facade.UserFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private AppointmentStatusHistoryRepository historyRepository;

    @Mock
    private DoctorFacade doctorFacade;

    @Mock
    private UserFacade userFacade;

    @Mock
    private ScheduleFacade scheduleFacade;

    @Mock
    private AppointmentValidator validator;

    @Mock
    private AppointmentStatusPolicy statusPolicy;

    @Mock
    private AppointmentMapper mapper;

    @Mock
    private EmailService emailService;

    @Mock
    private ScheduleCacheService scheduleCacheService;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private AppointmentServiceImpl appointmentService;

    private AppointmentCreateRequest createRequest;
    private AppointmentRescheduleRequest rescheduleRequest;
    private Appointment appointment;
    private Doctor doctor;
    private User patient;
    private DoctorWorkingPattern workingPattern;

    @BeforeEach
    void setUp() {
        LocalDateTime futureTime = LocalDateTime.now().plusDays(2);

        createRequest = new AppointmentCreateRequest();
        createRequest.setDoctorId(1L);
        createRequest.setStartDateTime(futureTime);
        createRequest.setNote("Test appointment");

        rescheduleRequest = new AppointmentRescheduleRequest();
        rescheduleRequest.setNewStartDatetime(futureTime.plusDays(1));

        patient = new User();
        patient.setId(2L);
        patient.setEmail("patient@example.com");
        patient.setIsActive(true);

        doctor = new Doctor();
        doctor.setId(1L);
        doctor.setConsultationFee(BigDecimal.valueOf(100));
        doctor.setUser(patient);

        User doctorUser = new User();
        doctorUser.setId(1L);
        doctorUser.setEmail("doctor@example.com");
        doctorUser.setIsActive(true);
        doctor.setUser(doctorUser);

        appointment = new Appointment();
        appointment.setId(1L);
        appointment.setBookingCode("ABC1234567");
        appointment.setDoctor(doctor);
        appointment.setPatient(patient);
        appointment.setStartDatetime(futureTime);
        appointment.setEndDatetime(futureTime.plusMinutes(30));
        appointment.setStatus(AppointmentStatus.PENDING);
        appointment.setConsultationFee(BigDecimal.valueOf(100));

        workingPattern = new DoctorWorkingPattern();
        workingPattern.setId(1L);
        workingPattern.setSlotDuration(30);
    }

    @Test
    void createAppointment_Success() {
        doNothing().when(validator).validateBookingTime(any(LocalDateTime.class));
        when(doctorFacade.getDoctorForBooking(1L)).thenReturn(doctor);
        when(userFacade.getCurrentUserEntity()).thenReturn(patient);
        when(scheduleFacade.getWorkingPattern(anyLong(), any(LocalDateTime.class))).thenReturn(workingPattern);
        when(scheduleFacade.isDoctorOnTimeOff(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(false);
        when(appointmentRepository.existsPatientOverlap(anyLong(), anyLong(), any(LocalDateTime.class),
                any(LocalDateTime.class))).thenReturn(false);
        when(appointmentRepository.existsActiveAppointment(anyLong(), any(LocalDateTime.class))).thenReturn(false);
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);
        when(mapper.toResponse(any(Appointment.class))).thenReturn(new AppointmentResponse());
        doNothing().when(scheduleCacheService).evictSlots(anyLong(), any(LocalDate.class));
        doNothing().when(auditService).log(anyString(), anyString(), anyLong(), any(), any());

        AppointmentResponse response = appointmentService.createAppointment(createRequest);

        assertThat(response).isNotNull();
        verify(appointmentRepository).save(any(Appointment.class));
        verify(historyRepository).save(any());
        verify(emailService).sendAppointmentCreatedEmail(any(AppointmentEmailData.class));
    }

    @Test
    void createAppointment_DoctorNotFound() {
        doNothing().when(validator).validateBookingTime(any(LocalDateTime.class));
        when(doctorFacade.getDoctorForBooking(1L)).thenReturn(null);

        assertThatThrownBy(() -> appointmentService.createAppointment(createRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Doctor not found");

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void createAppointment_DoctorDeleted() {
        doctor.setDeletedAt(LocalDateTime.now());
        doNothing().when(validator).validateBookingTime(any(LocalDateTime.class));
        when(doctorFacade.getDoctorForBooking(1L)).thenReturn(doctor);

        assertThatThrownBy(() -> appointmentService.createAppointment(createRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Doctor is deleted");

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void createAppointment_DoctorInactive() {
        doctor.getUser().setIsActive(false);
        doNothing().when(validator).validateBookingTime(any(LocalDateTime.class));
        when(doctorFacade.getDoctorForBooking(1L)).thenReturn(doctor);

        assertThatThrownBy(() -> appointmentService.createAppointment(createRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Doctor is inactive");

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void createAppointment_PatientInactive() {
        patient.setIsActive(false);
        doNothing().when(validator).validateBookingTime(any(LocalDateTime.class));
        when(doctorFacade.getDoctorForBooking(1L)).thenReturn(doctor);
        when(userFacade.getCurrentUserEntity()).thenReturn(patient);

        assertThatThrownBy(() -> appointmentService.createAppointment(createRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Account is inactive");

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void createAppointment_DoctorBookingSelf() {
        User doctorUser = doctor.getUser();
        doNothing().when(validator).validateBookingTime(any(LocalDateTime.class));
        when(doctorFacade.getDoctorForBooking(1L)).thenReturn(doctor);
        when(userFacade.getCurrentUserEntity()).thenReturn(doctorUser);

        assertThatThrownBy(() -> appointmentService.createAppointment(createRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Doctors cannot book appointments with themselves");

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void createAppointment_SlotAlreadyBooked() {
        doNothing().when(validator).validateBookingTime(any(LocalDateTime.class));
        when(doctorFacade.getDoctorForBooking(1L)).thenReturn(doctor);
        when(userFacade.getCurrentUserEntity()).thenReturn(patient);
        when(scheduleFacade.getWorkingPattern(anyLong(), any(LocalDateTime.class))).thenReturn(workingPattern);
        when(scheduleFacade.isDoctorOnTimeOff(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(false);
        when(appointmentRepository.existsPatientOverlap(anyLong(), anyLong(), any(LocalDateTime.class),
                any(LocalDateTime.class))).thenReturn(false);
        when(appointmentRepository.existsActiveAppointment(anyLong(), any(LocalDateTime.class))).thenReturn(true);

        assertThatThrownBy(() -> appointmentService.createAppointment(createRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Slot already booked");

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void createAppointment_PatientOverlap() {
        doNothing().when(validator).validateBookingTime(any(LocalDateTime.class));
        when(doctorFacade.getDoctorForBooking(1L)).thenReturn(doctor);
        when(userFacade.getCurrentUserEntity()).thenReturn(patient);
        when(scheduleFacade.getWorkingPattern(anyLong(), any(LocalDateTime.class))).thenReturn(workingPattern);
        when(scheduleFacade.isDoctorOnTimeOff(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(false);
        when(appointmentRepository.existsPatientOverlap(anyLong(), anyLong(), any(LocalDateTime.class),
                any(LocalDateTime.class))).thenReturn(true);
        doThrow(new BadRequestException("Patient has overlapping appointment")).when(validator)
                .validatePatientOverlap(true);

        assertThatThrownBy(() -> appointmentService.createAppointment(createRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Patient has overlapping appointment");

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void getAppointment_Success() {
        when(appointmentRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(appointment));
        when(mapper.toResponse(any(Appointment.class))).thenReturn(new AppointmentResponse());

        AppointmentResponse response = appointmentService.getAppointment(1L);

        assertThat(response).isNotNull();
        verify(appointmentRepository).findByIdAndDeletedAtIsNull(1L);
    }

    @Test
    void getAppointment_NotFound() {
        when(appointmentRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.getAppointment(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Appointment not found");
    }

    @Test
    void cancelAppointment_Success() {
        appointment.setStartDatetime(LocalDateTime.now().plusDays(2));
        when(appointmentRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(appointment));
        doNothing().when(statusPolicy).validateCancelTransition(any(AppointmentStatus.class));
        when(userFacade.getCurrentUserEntity()).thenReturn(patient);
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);
        doNothing().when(scheduleCacheService).evictSlots(anyLong(), any(LocalDate.class));
        doNothing().when(auditService).log(anyString(), anyString(), anyLong(), any(), any());

        appointmentService.cancelAppointment(1L, "Test cancellation");

        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CANCELLED);
        verify(appointmentRepository).save(appointment);
    }

    @Test
    void cancelAppointment_NotFound() {
        when(appointmentRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.cancelAppointment(1L, "Test"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Appointment not found");
    }

    @Test
    void cancelAppointment_InvalidStatus() {
        appointment.setStatus(AppointmentStatus.COMPLETED);
        when(appointmentRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(appointment));
        doThrow(new BadRequestException("Cannot cancel completed appointment")).when(statusPolicy)
                .validateCancelTransition(any(AppointmentStatus.class));

        assertThatThrownBy(() -> appointmentService.cancelAppointment(1L, "Test"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Cannot cancel completed appointment");

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void cancelAppointment_ReasonRequired() {
        appointment.setStartDatetime(LocalDateTime.now().plusDays(2));
        when(appointmentRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(appointment));
        doNothing().when(statusPolicy).validateCancelTransition(any(AppointmentStatus.class));

        assertThatThrownBy(() -> appointmentService.cancelAppointment(1L, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Cancellation reason is required");

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void cancelAppointment_LessThan24Hours() {
        appointment.setStartDatetime(LocalDateTime.now().plusHours(12));
        when(appointmentRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(appointment));
        doNothing().when(statusPolicy).validateCancelTransition(any(AppointmentStatus.class));

        assertThatThrownBy(() -> appointmentService.cancelAppointment(1L, "Test"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("at least 24 hours in advance");

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void confirmAppointment_Success() {
        appointment.setStartDatetime(LocalDateTime.now().plusDays(1));
        when(appointmentRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(appointment));
        doNothing().when(statusPolicy).validateConfirmTransition(any(AppointmentStatus.class));
        when(userFacade.getCurrentUserEntity()).thenReturn(patient);
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);
        doNothing().when(auditService).log(anyString(), anyString(), anyLong(), any(), any());

        appointmentService.confirmAppointment(1L);

        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
        verify(appointmentRepository).save(appointment);
    }

    @Test
    void confirmAppointment_AppointmentPassed() {
        appointment.setStartDatetime(LocalDateTime.now().minusHours(1));
        when(appointmentRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> appointmentService.confirmAppointment(1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already started or passed");

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void completeAppointment_Success() {
        appointment.setStartDatetime(LocalDateTime.now().minusHours(1));
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        when(appointmentRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(appointment));
        doNothing().when(statusPolicy).validateCompleteTransition(any(AppointmentStatus.class));
        when(userFacade.getCurrentUserEntity()).thenReturn(patient);
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);
        doNothing().when(auditService).log(anyString(), anyString(), anyLong(), any(), any());

        appointmentService.completeAppointment(1L);

        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.COMPLETED);
        verify(appointmentRepository).save(appointment);
    }

    @Test
    void completeAppointment_NotStarted() {
        appointment.setStartDatetime(LocalDateTime.now().plusHours(1));
        when(appointmentRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> appointmentService.completeAppointment(1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("not started yet");

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void markNoShow_Success() {
        appointment.setStartDatetime(LocalDateTime.now().minusHours(1));
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        when(appointmentRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(appointment));
        doNothing().when(statusPolicy).validateNoShowTransition(any(AppointmentStatus.class));
        when(userFacade.getCurrentUserEntity()).thenReturn(patient);
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);
        doNothing().when(auditService).log(anyString(), anyString(), anyLong(), any(), any());

        appointmentService.markNoShow(1L);

        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.NO_SHOW);
        verify(appointmentRepository).save(appointment);
    }

    @Test
    void markNoShow_BeforeAppointmentTime() {
        appointment.setStartDatetime(LocalDateTime.now().plusHours(1));
        when(appointmentRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> appointmentService.markNoShow(1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("before the appointment time");

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void rescheduleAppointment_Success() {
        appointment.setStartDatetime(LocalDateTime.now().plusDays(2));
        appointment.setStatus(AppointmentStatus.PENDING);
        when(appointmentRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(appointment));
        when(userFacade.getCurrentUserEntity()).thenReturn(patient);
        doNothing().when(statusPolicy).validateRescheduleTransition(any(AppointmentStatus.class));
        doNothing().when(validator).validateBookingTime(any(LocalDateTime.class));
        when(scheduleFacade.getWorkingPattern(anyLong(), any(LocalDateTime.class))).thenReturn(workingPattern);
        when(scheduleFacade.isDoctorOnTimeOff(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(false);
        when(appointmentRepository.existsPatientOverlap(anyLong(), anyLong(), any(LocalDateTime.class),
                any(LocalDateTime.class))).thenReturn(false);
        when(appointmentRepository.existsActiveAppointment(anyLong(), any(LocalDateTime.class))).thenReturn(false);
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);
        when(mapper.toResponse(any(Appointment.class))).thenReturn(new AppointmentResponse());
        doNothing().when(scheduleCacheService).evictSlots(anyLong(), any(LocalDate.class));
        doNothing().when(auditService).log(anyString(), anyString(), anyLong(), any(), any());

        AppointmentResponse response = appointmentService.rescheduleAppointment(1L, rescheduleRequest);

        assertThat(response).isNotNull();
        verify(appointmentRepository).save(appointment);
    }

    @Test
    void rescheduleAppointment_NotFound() {
        when(appointmentRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.rescheduleAppointment(1L, rescheduleRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Appointment not found");
    }

    @Test
    void rescheduleAppointment_InvalidStatus() {
        appointment.setStatus(AppointmentStatus.COMPLETED);
        when(appointmentRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(appointment));
        when(userFacade.getCurrentUserEntity()).thenReturn(patient);
        doThrow(new BadRequestException("Cannot reschedule")).when(statusPolicy)
                .validateRescheduleTransition(any(AppointmentStatus.class));

        assertThatThrownBy(() -> appointmentService.rescheduleAppointment(1L, rescheduleRequest))
                .isInstanceOf(BadRequestException.class);

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void rescheduleAppointment_LessThan24Hours() {
        appointment.setStartDatetime(LocalDateTime.now().plusHours(12));
        when(appointmentRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(appointment));
        when(userFacade.getCurrentUserEntity()).thenReturn(patient);
        doNothing().when(statusPolicy).validateRescheduleTransition(any(AppointmentStatus.class));

        assertThatThrownBy(() -> appointmentService.rescheduleAppointment(1L, rescheduleRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("at least 24 hours in advance");

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void rescheduleAppointment_SlotAlreadyBooked() {
        appointment.setStartDatetime(LocalDateTime.now().plusDays(2));
        when(appointmentRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(appointment));
        when(userFacade.getCurrentUserEntity()).thenReturn(patient);
        doNothing().when(statusPolicy).validateRescheduleTransition(any(AppointmentStatus.class));
        doNothing().when(validator).validateBookingTime(any(LocalDateTime.class));
        when(scheduleFacade.getWorkingPattern(anyLong(), any(LocalDateTime.class))).thenReturn(workingPattern);
        when(scheduleFacade.isDoctorOnTimeOff(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(false);
        when(appointmentRepository.existsPatientOverlap(anyLong(), anyLong(), any(LocalDateTime.class),
                any(LocalDateTime.class))).thenReturn(false);
        when(appointmentRepository.existsActiveAppointment(anyLong(), any(LocalDateTime.class))).thenReturn(true);

        assertThatThrownBy(() -> appointmentService.rescheduleAppointment(1L, rescheduleRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Slot already booked");

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void isSlotBooked_True() {
        when(appointmentRepository.existsActiveAppointment(1L, LocalDateTime.now().plusDays(1))).thenReturn(true);

        boolean result = appointmentService.isSlotBooked(1L, LocalDateTime.now().plusDays(1));

        assertThat(result).isTrue();
    }

    @Test
    void isSlotBooked_False() {
        when(appointmentRepository.existsActiveAppointment(1L, LocalDateTime.now().plusDays(1))).thenReturn(false);

        boolean result = appointmentService.isSlotBooked(1L, LocalDateTime.now().plusDays(1));

        assertThat(result).isFalse();
    }

    @Test
    void getMyAppointmentsStats_Success() {
        when(userFacade.getCurrentUserEntity()).thenReturn(patient);
        when(appointmentRepository.countPatientGroupByStatus(2L)).thenReturn(List.of());

        AppointmentStatsResponse stats = appointmentService.getMyAppointmentsStats();

        assertThat(stats).isNotNull();
        assertThat(stats.getTotal()).isEqualTo(0);
    }

    @Test
    void getAppointmentEntity_Success() {
        when(appointmentRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(appointment));

        Appointment result = appointmentService.getAppointmentEntity(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getAppointmentEntity_NotFound() {
        when(appointmentRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.getAppointmentEntity(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Appointment not found");
    }
}
