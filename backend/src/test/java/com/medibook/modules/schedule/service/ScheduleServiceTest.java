package com.medibook.modules.schedule.service;

import com.medibook.common.enums.DayOfWeekEnum;
import com.medibook.common.exception.BadRequestException;
import com.medibook.common.exception.ForbiddenException;
import com.medibook.common.exception.ResourceNotFoundException;
import com.medibook.modules.appointment.port.AppointmentSchedulePort;
import com.medibook.modules.doctor.entity.Doctor;
import com.medibook.modules.doctor.port.DoctorQueryPort;
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
import com.medibook.modules.schedule.service.impl.ScheduleServiceImpl;
import com.medibook.modules.schedule.validator.TimeOffValidator;
import com.medibook.modules.schedule.validator.WorkingPatternValidator;
import com.medibook.modules.user.entity.User;
import com.medibook.modules.audit.service.AuditService;
import com.medibook.security.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {

    @Mock
    private DoctorWorkingPatternRepository doctorWorkingPatternRepository;

    @Mock
    private DoctorTimeOffRepository doctorTimeOffRepository;

    @Mock
    private WorkingPatternValidator workingPatternValidator;

    @Mock
    private TimeOffValidator timeOffValidator;

    @Mock
    private ScheduleMapper scheduleMapper;

    @Mock
    private AppointmentSchedulePort appointmentSchedulePort;

    @Mock
    private DoctorQueryPort doctorQueryPort;

    @Mock
    private ScheduleCacheService scheduleCacheService;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private ScheduleServiceImpl scheduleService;

    private WorkingPatternRequest workingPatternRequest;
    private WorkingPatternUpdateRequest workingPatternUpdateRequest;
    private TimeOffRequest timeOffRequest;
    private TimeOffUpdateRequest timeOffUpdateRequest;
    private SlotGenerateRequest slotGenerateRequest;
    private DoctorWorkingPattern workingPattern;
    private DoctorTimeOff timeOff;
    private Doctor doctor;
    private Authentication authentication;
    private LocalDateTime futureTime;

    @BeforeEach
    void setUp() {
        futureTime = LocalDateTime.now().plusDays(1);

        workingPatternRequest = new WorkingPatternRequest();
        workingPatternRequest.setDoctorId(1L);
        workingPatternRequest.setDayOfWeek(DayOfWeekEnum.MON);
        workingPatternRequest.setStartTime(LocalTime.of(9, 0));
        workingPatternRequest.setEndTime(LocalTime.of(17, 0));
        workingPatternRequest.setSlotDuration(30);
        workingPatternRequest.setBufferDuration(5);

        workingPatternUpdateRequest = new WorkingPatternUpdateRequest();
        workingPatternUpdateRequest.setDayOfWeek(DayOfWeekEnum.TUE);
        workingPatternUpdateRequest.setStartTime(LocalTime.of(10, 0));
        workingPatternUpdateRequest.setEndTime(LocalTime.of(18, 0));
        workingPatternUpdateRequest.setSlotDuration(45);
        workingPatternUpdateRequest.setBufferDuration(10);

        timeOffRequest = new TimeOffRequest();
        timeOffRequest.setDoctorId(1L);
        timeOffRequest.setStartDatetime(futureTime);
        timeOffRequest.setEndDatetime(futureTime.plusHours(2));
        timeOffRequest.setReason("Personal leave");

        timeOffUpdateRequest = new TimeOffUpdateRequest();
        timeOffUpdateRequest.setStartDatetime(futureTime.plusDays(1));
        timeOffUpdateRequest.setEndDatetime(futureTime.plusDays(1).plusHours(3));
        timeOffUpdateRequest.setReason("Updated reason");

        slotGenerateRequest = SlotGenerateRequest.builder()
                .doctorId(1L)
                .date(futureTime.toLocalDate())
                .build();

        doctor = new Doctor();
        doctor.setId(1L);

        User doctorUser = new User();
        doctorUser.setId(1L);
        doctor.setUser(doctorUser);

        workingPattern = new DoctorWorkingPattern();
        workingPattern.setId(1L);
        workingPattern.setDoctor(doctor);
        workingPattern.setDayOfWeek(DayOfWeekEnum.MON);
        workingPattern.setStartTime(LocalTime.of(9, 0));
        workingPattern.setEndTime(LocalTime.of(17, 0));
        workingPattern.setSlotDuration(30);
        workingPattern.setBufferDuration(5);

        timeOff = new DoctorTimeOff();
        timeOff.setId(1L);
        timeOff.setDoctor(doctor);
        timeOff.setStartDatetime(futureTime);
        timeOff.setEndDatetime(futureTime.plusHours(2));
        timeOff.setReason("Personal leave");

        authentication = mock(Authentication.class);
    }

    @Test
    void createWorkingPattern_Success() {
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class);
                MockedStatic<SecurityContextHolder> contextHolder = mockStatic(SecurityContextHolder.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(1L);
            contextHolder.when(SecurityContextHolder::getContext)
                    .thenReturn(mock(org.springframework.security.core.context.SecurityContext.class));

            doNothing().when(workingPatternValidator).validate(any(WorkingPatternRequest.class));
            when(doctorQueryPort.getDoctor(1L)).thenReturn(doctor);
            when(doctorWorkingPatternRepository.existsByDoctorIdAndDayOfWeekAndDeletedAtIsNull(1L,
                    DayOfWeekEnum.MON)).thenReturn(false);
            when(scheduleMapper.toEntity(any(WorkingPatternRequest.class))).thenReturn(workingPattern);
            when(doctorWorkingPatternRepository.save(any(DoctorWorkingPattern.class))).thenReturn(workingPattern);
            when(scheduleMapper.toResponse(any(DoctorWorkingPattern.class))).thenReturn(
                WorkingPatternResponse.builder()
                    .id(1L)
                    .doctorId(1L)
                    .dayOfWeek(DayOfWeekEnum.MON)
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(17, 0))
                    .slotDuration(30)
                    .bufferDuration(5)
                    .build()
            );
            doNothing().when(scheduleCacheService).clearAllSlots();
            doNothing().when(auditService).log(anyString(), anyString(), anyLong(), any(), any());

            WorkingPatternResponse response = scheduleService.createWorkingPattern(workingPatternRequest);

            assertThat(response).isNotNull();
            verify(doctorWorkingPatternRepository).save(any(DoctorWorkingPattern.class));
            verify(auditService).log(eq("CREATE"), eq("WorkingPattern"), anyLong(), any(), any());
        }
    }

    @Test
    void createWorkingPattern_AlreadyExists() {
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class);
                MockedStatic<SecurityContextHolder> contextHolder = mockStatic(SecurityContextHolder.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(1L);
            contextHolder.when(SecurityContextHolder::getContext)
                    .thenReturn(mock(org.springframework.security.core.context.SecurityContext.class));

            doNothing().when(workingPatternValidator).validate(any(WorkingPatternRequest.class));
            when(doctorQueryPort.getDoctor(1L)).thenReturn(doctor);
            when(doctorWorkingPatternRepository.existsByDoctorIdAndDayOfWeekAndDeletedAtIsNull(1L,
                    DayOfWeekEnum.MON)).thenReturn(true);

            assertThatThrownBy(() -> scheduleService.createWorkingPattern(workingPatternRequest))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Working pattern already exists for this day");

            verify(doctorWorkingPatternRepository, never()).save(any(DoctorWorkingPattern.class));
        }
    }

    @Test
    void updateWorkingPattern_Success() {
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class);
                MockedStatic<SecurityContextHolder> contextHolder = mockStatic(SecurityContextHolder.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(1L);
            contextHolder.when(SecurityContextHolder::getContext)
                    .thenReturn(mock(org.springframework.security.core.context.SecurityContext.class));

            when(doctorWorkingPatternRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(workingPattern));
            doNothing().when(workingPatternValidator).validateUpdate(any(DoctorWorkingPattern.class),
                    any(WorkingPatternUpdateRequest.class));
            when(appointmentSchedulePort.hasFutureAppointments(eq(1L), any(LocalDateTime.class))).thenReturn(false);
            when(doctorWorkingPatternRepository.save(any(DoctorWorkingPattern.class))).thenReturn(workingPattern);
            when(scheduleMapper.toResponse(any(DoctorWorkingPattern.class))).thenReturn(
                WorkingPatternResponse.builder()
                    .id(1L)
                    .doctorId(1L)
                    .dayOfWeek(DayOfWeekEnum.MON)
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(17, 0))
                    .slotDuration(30)
                    .bufferDuration(5)
                    .build()
            );
            doNothing().when(scheduleCacheService).clearAllSlots();
            doNothing().when(auditService).log(anyString(), anyString(), anyLong(), any(), any());

            WorkingPatternResponse response = scheduleService.updateWorkingPattern(1L, workingPatternUpdateRequest);

            assertThat(response).isNotNull();
            verify(doctorWorkingPatternRepository).save(workingPattern);
            verify(auditService).log(eq("UPDATE"), eq("WorkingPattern"), anyLong(), any(), any());
        }
    }

    @Test
    void updateWorkingPattern_NotFound() {
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class);
                MockedStatic<SecurityContextHolder> contextHolder = mockStatic(SecurityContextHolder.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(1L);
            contextHolder.when(SecurityContextHolder::getContext)
                    .thenReturn(mock(org.springframework.security.core.context.SecurityContext.class));

            when(doctorWorkingPatternRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> scheduleService.updateWorkingPattern(1L, workingPatternUpdateRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Working pattern not found");

            verify(doctorWorkingPatternRepository, never()).save(any(DoctorWorkingPattern.class));
        }
    }

    @Test
    void updateWorkingPattern_FutureAppointmentsExist() {
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class);
                MockedStatic<SecurityContextHolder> contextHolder = mockStatic(SecurityContextHolder.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(1L);
            contextHolder.when(SecurityContextHolder::getContext)
                    .thenReturn(mock(org.springframework.security.core.context.SecurityContext.class));

            when(doctorWorkingPatternRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(workingPattern));
            doNothing().when(workingPatternValidator).validateUpdate(any(DoctorWorkingPattern.class),
                    any(WorkingPatternUpdateRequest.class));
            when(appointmentSchedulePort.hasFutureAppointments(eq(1L), any(LocalDateTime.class))).thenReturn(true);

            assertThatThrownBy(() -> scheduleService.updateWorkingPattern(1L, workingPatternUpdateRequest))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("future appointments exist");

            verify(doctorWorkingPatternRepository, never()).save(any(DoctorWorkingPattern.class));
        }
    }

    @Test
    void deleteWorkingPattern_Success() {
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class);
                MockedStatic<SecurityContextHolder> contextHolder = mockStatic(SecurityContextHolder.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(1L);
            contextHolder.when(SecurityContextHolder::getContext)
                    .thenReturn(mock(org.springframework.security.core.context.SecurityContext.class));

            when(doctorWorkingPatternRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(workingPattern));
            when(appointmentSchedulePort.hasFutureAppointments(eq(1L), any(LocalDateTime.class))).thenReturn(false);
            when(doctorWorkingPatternRepository.save(any(DoctorWorkingPattern.class))).thenReturn(workingPattern);
            doNothing().when(scheduleCacheService).clearAllSlots();
            doNothing().when(auditService).log(anyString(), anyString(), anyLong(), any(), any());

            scheduleService.deleteWorkingPattern(1L);

            assertThat(workingPattern.getDeletedAt()).isNotNull();
            verify(doctorWorkingPatternRepository).save(workingPattern);
            verify(auditService).log(eq("DELETE"), eq("WorkingPattern"), anyLong(), any(), any());
        }
    }

    @Test
    void deleteWorkingPattern_NotFound() {
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class);
                MockedStatic<SecurityContextHolder> contextHolder = mockStatic(SecurityContextHolder.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(1L);
            contextHolder.when(SecurityContextHolder::getContext)
                    .thenReturn(mock(org.springframework.security.core.context.SecurityContext.class));

            when(doctorWorkingPatternRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> scheduleService.deleteWorkingPattern(1L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Working pattern not found");

            verify(doctorWorkingPatternRepository, never()).save(any(DoctorWorkingPattern.class));
        }
    }

    @Test
    void createTimeOff_Success() {
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class);
                MockedStatic<SecurityContextHolder> contextHolder = mockStatic(SecurityContextHolder.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(1L);
            contextHolder.when(SecurityContextHolder::getContext)
                    .thenReturn(mock(org.springframework.security.core.context.SecurityContext.class));

            doNothing().when(timeOffValidator).validate(any(TimeOffRequest.class));
            when(doctorQueryPort.getDoctor(1L)).thenReturn(doctor);
            when(doctorWorkingPatternRepository.existsByDoctorIdAndDayOfWeekAndDeletedAtIsNull(1L,
                    any(DayOfWeekEnum.class))).thenReturn(true);
            when(doctorTimeOffRepository.findOverlapping(1L, any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(List.of());
            when(scheduleMapper.toEntity(any(TimeOffRequest.class))).thenReturn(timeOff);
            when(doctorTimeOffRepository.save(any(DoctorTimeOff.class))).thenReturn(timeOff);
            when(scheduleMapper.toResponse(any(DoctorTimeOff.class))).thenReturn(
                TimeOffResponse.builder()
                    .id(1L)
                    .doctorId(1L)
                    .startDateTime(futureTime)
                    .endDateTime(futureTime.plusHours(2))
                    .reason("Personal leave")
                    .build()
            );
            doNothing().when(scheduleCacheService).clearAllSlots();
            doNothing().when(auditService).log(anyString(), anyString(), anyLong(), any(), any());

            TimeOffResponse response = scheduleService.createTimeOff(timeOffRequest);

            assertThat(response).isNotNull();
            verify(doctorTimeOffRepository).save(any(DoctorTimeOff.class));
            verify(auditService).log(eq("CREATE"), eq("TimeOff"), anyLong(), any(), any());
        }
    }

    @Test
    void createTimeOff_NoWorkingPattern() {
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class);
                MockedStatic<SecurityContextHolder> contextHolder = mockStatic(SecurityContextHolder.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(1L);
            contextHolder.when(SecurityContextHolder::getContext)
                    .thenReturn(mock(org.springframework.security.core.context.SecurityContext.class));

            doNothing().when(timeOffValidator).validate(any(TimeOffRequest.class));
            when(doctorQueryPort.getDoctor(1L)).thenReturn(doctor);
            when(doctorWorkingPatternRepository.existsByDoctorIdAndDayOfWeekAndDeletedAtIsNull(1L,
                    any(DayOfWeekEnum.class))).thenReturn(false);

            assertThatThrownBy(() -> scheduleService.createTimeOff(timeOffRequest))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("không có lịch làm việc");

            verify(doctorTimeOffRepository, never()).save(any(DoctorTimeOff.class));
        }
    }

    @Test
    void createTimeOff_ExactDuplicate() {
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class);
                MockedStatic<SecurityContextHolder> contextHolder = mockStatic(SecurityContextHolder.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(1L);
            contextHolder.when(SecurityContextHolder::getContext)
                    .thenReturn(mock(org.springframework.security.core.context.SecurityContext.class));

            doNothing().when(timeOffValidator).validate(any(TimeOffRequest.class));
            when(doctorQueryPort.getDoctor(1L)).thenReturn(doctor);
            when(doctorWorkingPatternRepository.existsByDoctorIdAndDayOfWeekAndDeletedAtIsNull(1L,
                    any(DayOfWeekEnum.class))).thenReturn(true);
            when(doctorTimeOffRepository.findOverlapping(1L, any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(List.of(timeOff));
            when(scheduleMapper.toResponse(any(DoctorTimeOff.class))).thenReturn(
                TimeOffResponse.builder()
                    .id(1L)
                    .doctorId(1L)
                    .startDateTime(futureTime)
                    .endDateTime(futureTime.plusHours(2))
                    .reason("Personal leave")
                    .build()
            );

            TimeOffResponse response = scheduleService.createTimeOff(timeOffRequest);

            assertThat(response).isNotNull();
            verify(doctorTimeOffRepository, never()).save(any(DoctorTimeOff.class));
        }
    }

    @Test
    void updateTimeOff_Success() {
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class);
                MockedStatic<SecurityContextHolder> contextHolder = mockStatic(SecurityContextHolder.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(1L);
            contextHolder.when(SecurityContextHolder::getContext)
                    .thenReturn(mock(org.springframework.security.core.context.SecurityContext.class));

            when(doctorTimeOffRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(timeOff));
            when(doctorTimeOffRepository.save(any(DoctorTimeOff.class))).thenReturn(timeOff);
            when(scheduleMapper.toResponse(any(DoctorTimeOff.class))).thenReturn(
                TimeOffResponse.builder()
                    .id(1L)
                    .doctorId(1L)
                    .startDateTime(futureTime)
                    .endDateTime(futureTime.plusHours(2))
                    .reason("Personal leave")
                    .build()
            );
            doNothing().when(scheduleCacheService).clearAllSlots();
            doNothing().when(auditService).log(anyString(), anyString(), anyLong(), any(), any());

            TimeOffResponse response = scheduleService.updateTimeOff(1L, timeOffUpdateRequest);

            assertThat(response).isNotNull();
            verify(doctorTimeOffRepository).save(timeOff);
            verify(auditService).log(eq("UPDATE"), eq("TimeOff"), anyLong(), any(), any());
        }
    }

    @Test
    void updateTimeOff_Ongoing() {
        timeOff.setStartDatetime(LocalDateTime.now().minusHours(1));
        timeOff.setEndDatetime(LocalDateTime.now().plusHours(1));

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class);
                MockedStatic<SecurityContextHolder> contextHolder = mockStatic(SecurityContextHolder.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(1L);
            contextHolder.when(SecurityContextHolder::getContext)
                    .thenReturn(mock(org.springframework.security.core.context.SecurityContext.class));

            when(doctorTimeOffRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(timeOff));

            assertThatThrownBy(() -> scheduleService.updateTimeOff(1L, timeOffUpdateRequest))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Cannot update ongoing time off");

            verify(doctorTimeOffRepository, never()).save(any(DoctorTimeOff.class));
        }
    }

    @Test
    void deleteTimeOff_Success() {
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class);
                MockedStatic<SecurityContextHolder> contextHolder = mockStatic(SecurityContextHolder.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(1L);
            contextHolder.when(SecurityContextHolder::getContext)
                    .thenReturn(mock(org.springframework.security.core.context.SecurityContext.class));

            when(doctorTimeOffRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(timeOff));
            when(doctorTimeOffRepository.save(any(DoctorTimeOff.class))).thenReturn(timeOff);
            doNothing().when(scheduleCacheService).clearAllSlots();
            doNothing().when(auditService).log(anyString(), anyString(), anyLong(), any(), any());

            scheduleService.deleteTimeOff(1L);

            assertThat(timeOff.getDeletedAt()).isNotNull();
            verify(doctorTimeOffRepository).save(timeOff);
            verify(auditService).log(eq("DELETE"), eq("TimeOff"), anyLong(), any(), any());
        }
    }

    @Test
    void deleteTimeOff_Ongoing() {
        timeOff.setStartDatetime(LocalDateTime.now().minusHours(1));
        timeOff.setEndDatetime(LocalDateTime.now().plusHours(1));

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class);
                MockedStatic<SecurityContextHolder> contextHolder = mockStatic(SecurityContextHolder.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(1L);
            contextHolder.when(SecurityContextHolder::getContext)
                    .thenReturn(mock(org.springframework.security.core.context.SecurityContext.class));

            when(doctorTimeOffRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(timeOff));

            assertThatThrownBy(() -> scheduleService.deleteTimeOff(1L))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Cannot delete ongoing time off");

            verify(doctorTimeOffRepository, never()).save(any(DoctorTimeOff.class));
        }
    }

    @Test
    void generateSlots_Success() {
        List<SlotResponse> slots = List.of(
            SlotResponse.builder()
                .start(futureTime)
                .end(futureTime.plusMinutes(30))
                .available(true)
                .build()
        );
        when(scheduleCacheService.getSlots(1L, slotGenerateRequest.getDate())).thenReturn(slots);

        List<SlotResponse> response = scheduleService.generateSlots(slotGenerateRequest);

        assertThat(response).isNotNull();
        assertThat(response).hasSize(1);
        verify(scheduleCacheService).getSlots(1L, slotGenerateRequest.getDate());
    }

    @Test
    void getDoctorSchedule_Success() {
        when(doctorQueryPort.getDoctor(1L)).thenReturn(doctor);
        when(doctorWorkingPatternRepository.findByDoctorIdAndDeletedAtIsNull(1L)).thenReturn(List.of(workingPattern));
        when(doctorTimeOffRepository.findByDoctorIdAndDeletedAtIsNull(1L)).thenReturn(List.of(timeOff));
        when(scheduleMapper.toResponse(any(DoctorWorkingPattern.class))).thenReturn(
                WorkingPatternResponse.builder()
                    .id(1L)
                    .doctorId(1L)
                    .dayOfWeek(DayOfWeekEnum.MON)
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(17, 0))
                    .slotDuration(30)
                    .bufferDuration(5)
                    .build()
            );
        when(scheduleMapper.toResponse(any(DoctorTimeOff.class))).thenReturn(
                TimeOffResponse.builder()
                    .id(1L)
                    .doctorId(1L)
                    .startDateTime(futureTime)
                    .endDateTime(futureTime.plusHours(2))
                    .reason("Personal leave")
                    .build()
            );

        DoctorScheduleResponse response = scheduleService.getDoctorSchedule(1L);

        assertThat(response).isNotNull();
        assertThat(response.getDoctorId()).isEqualTo(1L);
        verify(doctorQueryPort).getDoctor(1L);
    }
}
