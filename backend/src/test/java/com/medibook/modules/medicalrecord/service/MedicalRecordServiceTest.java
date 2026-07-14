package com.medibook.modules.medicalrecord.service;

import com.medibook.common.enums.AppointmentStatus;
import com.medibook.common.exception.BadRequestException;
import com.medibook.common.exception.ForbiddenException;
import com.medibook.common.exception.ResourceNotFoundException;
import com.medibook.modules.appointment.entity.Appointment;
import com.medibook.modules.appointment.facade.AppointmentFacade;
import com.medibook.modules.medicalrecord.dto.request.MedicalRecordCreateRequest;
import com.medibook.modules.medicalrecord.dto.request.MedicalRecordUpdateRequest;
import com.medibook.modules.medicalrecord.dto.response.MedicalRecordResponse;
import com.medibook.modules.medicalrecord.entity.MedicalRecord;
import com.medibook.modules.medicalrecord.mapper.MedicalRecordMapper;
import com.medibook.modules.medicalrecord.repository.MedicalRecordRepository;
import com.medibook.modules.medicalrecord.service.impl.MedicalRecordServiceImpl;
import com.medibook.modules.medicalrecord.validator.MedicalRecordSecurityValidator;
import com.medibook.modules.medicalrecord.validator.MedicalRecordValidator;
import com.medibook.modules.user.dto.internal.CurrentUserDto;
import com.medibook.modules.user.entity.User;
import com.medibook.modules.user.facade.UserFacade;
import com.medibook.modules.audit.service.AuditService;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicalRecordServiceTest {

    @Mock
    private MedicalRecordRepository medicalRecordRepository;

    @Mock
    private MedicalRecordMapper medicalRecordMapper;

    @Mock
    private MedicalRecordValidator validator;

    @Mock
    private MedicalRecordSecurityValidator securityValidator;

    @Mock
    private AppointmentFacade appointmentFacade;

    @Mock
    private UserFacade userFacade;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private MedicalRecordServiceImpl medicalRecordService;

    private MedicalRecordCreateRequest createRequest;
    private MedicalRecordUpdateRequest updateRequest;
    private MedicalRecord medicalRecord;
    private Appointment appointment;
    private User doctor;
    private User patient;
    private CurrentUserDto currentUser;

    @BeforeEach
    void setUp() {
        createRequest = new MedicalRecordCreateRequest();
        createRequest.setAppointmentId(1L);
        createRequest.setDiagnosis("Test diagnosis");
        createRequest.setPrescription("Test prescription");
        createRequest.setNote("Test note");

        updateRequest = new MedicalRecordUpdateRequest();
        updateRequest.setDiagnosis("Updated diagnosis");
        updateRequest.setPrescription("Updated prescription");
        updateRequest.setNote("Updated note");

        doctor = new User();
        doctor.setId(1L);
        doctor.setEmail("doctor@example.com");

        patient = new User();
        patient.setId(2L);
        patient.setEmail("patient@example.com");

        com.medibook.modules.doctor.entity.Doctor doctorEntity = new com.medibook.modules.doctor.entity.Doctor();
        doctorEntity.setId(1L);
        doctorEntity.setUser(doctor);

        appointment = new Appointment();
        appointment.setId(1L);
        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointment.setPatient(patient);
        appointment.setDoctor(doctorEntity);

        medicalRecord = new MedicalRecord();
        medicalRecord.setId(1L);
        medicalRecord.setAppointment(appointment);
        medicalRecord.setDiagnosis("Test diagnosis");
        medicalRecord.setPrescription("Test prescription");
        medicalRecord.setNote("Test note");

        currentUser = new CurrentUserDto(1L, "doctor@example.com");
    }

    @Test
    void create_Success() {
        when(appointmentFacade.getAppointmentEntity(1L)).thenReturn(appointment);
        doNothing().when(validator).validateCreate(anyLong(), any(AppointmentStatus.class), anyBoolean());
        when(userFacade.getCurrentUser()).thenReturn(currentUser);
        doNothing().when(securityValidator).validateDoctorOwnership(1L, 1L);
        when(medicalRecordMapper.toEntity(any(MedicalRecordCreateRequest.class))).thenReturn(medicalRecord);
        when(medicalRecordRepository.save(any(MedicalRecord.class))).thenReturn(medicalRecord);
        when(medicalRecordMapper.toResponse(any(MedicalRecord.class))).thenReturn(
                MedicalRecordResponse.builder()
                        .id(1L)
                        .appointmentId(1L)
                        .diagnosis("Test diagnosis")
                        .prescription("Test prescription")
                        .note("Test note")
                        .build());
        doNothing().when(auditService).log(anyString(), anyString(), anyLong(), any(), any());

        MedicalRecordResponse response = medicalRecordService.create(createRequest);

        assertThat(response).isNotNull();
        verify(medicalRecordRepository).save(any(MedicalRecord.class));
        verify(auditService).log(eq("CREATE"), eq("MedicalRecord"), anyLong(), any(), any());
    }

    @Test
    void create_AppointmentNotCompleted() {
        appointment.setStatus(AppointmentStatus.PENDING);
        when(appointmentFacade.getAppointmentEntity(1L)).thenReturn(appointment);
        doThrow(new RuntimeException("Appointment not completed")).when(validator).validateCreate(anyLong(),
                any(AppointmentStatus.class), anyBoolean());

        assertThatThrownBy(() -> medicalRecordService.create(createRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Appointment not completed");

        verify(medicalRecordRepository, never()).save(any(MedicalRecord.class));
    }

    @Test
    void create_RecordAlreadyExists() {
        when(appointmentFacade.getAppointmentEntity(1L)).thenReturn(appointment);
        doNothing().when(validator).validateCreate(anyLong(), any(AppointmentStatus.class), anyBoolean());
        when(userFacade.getCurrentUser()).thenReturn(currentUser);
        doNothing().when(securityValidator).validateDoctorOwnership(1L, 1L);
        when(medicalRecordRepository.existsByAppointmentIdAndDeletedAtIsNull(1L)).thenReturn(true);
        doThrow(new RuntimeException("Record already exists")).when(validator).validateCreate(anyLong(),
                any(AppointmentStatus.class), anyBoolean());

        assertThatThrownBy(() -> medicalRecordService.create(createRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Record already exists");

        verify(medicalRecordRepository, never()).save(any(MedicalRecord.class));
    }

    @Test
    void create_AccessDenied() {
        when(appointmentFacade.getAppointmentEntity(1L)).thenReturn(appointment);
        doNothing().when(validator).validateCreate(anyLong(), any(AppointmentStatus.class), anyBoolean());
        when(userFacade.getCurrentUser()).thenReturn(currentUser);
        doThrow(new ForbiddenException("Access denied")).when(securityValidator).validateDoctorOwnership(1L, 1L);

        assertThatThrownBy(() -> medicalRecordService.create(createRequest))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("Access denied");

        verify(medicalRecordRepository, never()).save(any(MedicalRecord.class));
    }

    @Test
    void update_Success() {
        when(medicalRecordRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(medicalRecord));
        when(userFacade.getCurrentUser()).thenReturn(currentUser);
        doNothing().when(securityValidator).validateDoctorOwnership(1L, 1L);
        doNothing().when(medicalRecordMapper).updateEntity(any(MedicalRecordUpdateRequest.class),
                any(MedicalRecord.class));
        when(medicalRecordMapper.toResponse(any(MedicalRecord.class))).thenReturn(
                MedicalRecordResponse.builder()
                        .id(1L)
                        .appointmentId(1L)
                        .diagnosis("Test diagnosis")
                        .prescription("Test prescription")
                        .note("Test note")
                        .build());
        doNothing().when(auditService).log(anyString(), anyString(), anyLong(), any(), any());

        MedicalRecordResponse response = medicalRecordService.update(1L, updateRequest);

        assertThat(response).isNotNull();
        verify(auditService).log(eq("UPDATE"), eq("MedicalRecord"), anyLong(), any(), any());
    }

    @Test
    void update_NotFound() {
        when(medicalRecordRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> medicalRecordService.update(1L, updateRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Medical record not found");

        verify(medicalRecordRepository, never()).save(any(MedicalRecord.class));
    }

    @Test
    void update_AccessDenied() {
        when(medicalRecordRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(medicalRecord));
        when(userFacade.getCurrentUser()).thenReturn(currentUser);
        doThrow(new ForbiddenException("Access denied")).when(securityValidator).validateDoctorOwnership(1L, 1L);

        assertThatThrownBy(() -> medicalRecordService.update(1L, updateRequest))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("Access denied");

        verify(medicalRecordRepository, never()).save(any(MedicalRecord.class));
    }

    @Test
    void getById_Success() {
        when(medicalRecordRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(medicalRecord));
        when(userFacade.getCurrentUser()).thenReturn(currentUser);
        doNothing().when(securityValidator).validateViewPermission(anyLong(), anyLong(), anyLong(), anyBoolean());
        when(medicalRecordMapper.toResponse(any(MedicalRecord.class))).thenReturn(
                MedicalRecordResponse.builder()
                        .id(1L)
                        .appointmentId(1L)
                        .diagnosis("Test diagnosis")
                        .prescription("Test prescription")
                        .note("Test note")
                        .build());

        MedicalRecordResponse response = medicalRecordService.getById(1L);

        assertThat(response).isNotNull();
        verify(medicalRecordRepository).findByIdAndDeletedAtIsNull(1L);
    }

    @Test
    void getById_NotFound() {
        when(medicalRecordRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> medicalRecordService.getById(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Medical record not found");
    }

    @Test
    void getById_AccessDenied() {
        when(medicalRecordRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(medicalRecord));
        when(userFacade.getCurrentUser()).thenReturn(currentUser);
        doThrow(new RuntimeException("Access denied")).when(securityValidator).validateViewPermission(anyLong(),
                anyLong(), anyLong(), anyBoolean());

        assertThatThrownBy(() -> medicalRecordService.getById(1L))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("Access denied");
    }

    @Test
    void getMyMedicalRecords_Success() {
        currentUser = new CurrentUserDto(2L, "patient@example.com");
        Pageable pageable = PageRequest.of(0, 10);
        Page<MedicalRecord> recordPage = new PageImpl<>(List.of(medicalRecord));

        when(userFacade.getCurrentUser()).thenReturn(currentUser);
        when(medicalRecordRepository.findByAppointmentPatientIdAndDeletedAtIsNull(2L, pageable)).thenReturn(recordPage);
        when(medicalRecordMapper.toResponse(any(MedicalRecord.class))).thenReturn(
                MedicalRecordResponse.builder()
                        .id(1L)
                        .appointmentId(1L)
                        .diagnosis("Test diagnosis")
                        .prescription("Test prescription")
                        .note("Test note")
                        .build());

        Page<MedicalRecordResponse> response = medicalRecordService.getMyMedicalRecords(pageable);

        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        verify(medicalRecordRepository).findByAppointmentPatientIdAndDeletedAtIsNull(2L, pageable);
    }

    @Test
    void delete_Success() {
        when(medicalRecordRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(medicalRecord));
        when(userFacade.getCurrentUser()).thenReturn(currentUser);
        doNothing().when(securityValidator).validateDoctorOwnership(1L, 1L);
        when(medicalRecordRepository.save(any(MedicalRecord.class))).thenReturn(medicalRecord);
        doNothing().when(auditService).log(anyString(), anyString(), anyLong(), any(), any());

        medicalRecordService.delete(1L);

        assertThat(medicalRecord.getDeletedAt()).isNotNull();
        verify(medicalRecordRepository).save(medicalRecord);
        verify(auditService).log(eq("DELETE"), eq("MedicalRecord"), anyLong(), any(), any());
    }

    @Test
    void delete_NotFound() {
        when(medicalRecordRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> medicalRecordService.delete(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Medical record not found");

        verify(medicalRecordRepository, never()).save(any(MedicalRecord.class));
    }

    @Test
    void delete_AccessDenied() {
        when(medicalRecordRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(medicalRecord));
        when(userFacade.getCurrentUser()).thenReturn(currentUser);
        doThrow(new ForbiddenException("Access denied")).when(securityValidator).validateDoctorOwnership(1L, 1L);

        assertThatThrownBy(() -> medicalRecordService.delete(1L))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("Access denied");

        verify(medicalRecordRepository, never()).save(any(MedicalRecord.class));
    }

    @Test
    void getDoctorMedicalRecords_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<MedicalRecord> recordPage = new PageImpl<>(List.of(medicalRecord));

        when(userFacade.getCurrentUser()).thenReturn(currentUser);
        when(medicalRecordRepository.findByAppointmentDoctorUserIdAndDeletedAtIsNull(1L, pageable))
                .thenReturn(recordPage);
        when(medicalRecordMapper.toResponse(any(MedicalRecord.class))).thenReturn(
                MedicalRecordResponse.builder()
                        .id(1L)
                        .appointmentId(1L)
                        .diagnosis("Test diagnosis")
                        .prescription("Test prescription")
                        .note("Test note")
                        .build());

        Page<MedicalRecordResponse> response = medicalRecordService.getDoctorMedicalRecords(pageable);

        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        verify(medicalRecordRepository).findByAppointmentDoctorUserIdAndDeletedAtIsNull(1L, pageable);
    }

    @Test
    void getAllMedicalRecords_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<MedicalRecord> recordPage = new PageImpl<>(List.of(medicalRecord));

        when(medicalRecordRepository.findByDeletedAtIsNull(pageable)).thenReturn(recordPage);
        when(medicalRecordMapper.toResponse(any(MedicalRecord.class))).thenReturn(
                MedicalRecordResponse.builder()
                        .id(1L)
                        .appointmentId(1L)
                        .diagnosis("Test diagnosis")
                        .prescription("Test prescription")
                        .note("Test note")
                        .build());

        Page<MedicalRecordResponse> response = medicalRecordService.getAllMedicalRecords(pageable);

        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        verify(medicalRecordRepository).findByDeletedAtIsNull(pageable);
    }
}
