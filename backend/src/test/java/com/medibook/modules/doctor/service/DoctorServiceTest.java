package com.medibook.modules.doctor.service;

import com.medibook.common.constant.RoleConstants;
import com.medibook.common.exception.BadRequestException;
import com.medibook.common.exception.ForbiddenException;
import com.medibook.common.exception.ResourceNotFoundException;
import com.medibook.common.response.PageResponse;
import com.medibook.modules.doctor.dto.request.CreateDoctorRequest;
import com.medibook.modules.doctor.dto.request.DoctorSearchRequest;
import com.medibook.modules.doctor.dto.request.UpdateDoctorRequest;
import com.medibook.modules.doctor.dto.request.UpgradeToDoctorRequest;
import com.medibook.modules.doctor.dto.response.DoctorResponse;
import com.medibook.modules.doctor.dto.response.DoctorSummaryResponse;
import com.medibook.modules.doctor.entity.Doctor;
import com.medibook.modules.doctor.mapper.DoctorMapper;
import com.medibook.modules.doctor.repository.DoctorRepository;
import com.medibook.modules.doctor.service.impl.DoctorServiceImpl;
import com.medibook.modules.doctor.specification.DoctorSpecification;
import com.medibook.modules.doctor.validator.DoctorValidator;
import com.medibook.modules.specialty.entity.Specialty;
import com.medibook.modules.specialty.facade.SpecialtyFacade;
import com.medibook.modules.user.dto.internal.CurrentUserDto;
import com.medibook.modules.user.entity.Role;
import com.medibook.modules.user.entity.User;
import com.medibook.modules.user.facade.UserFacade;
import com.medibook.modules.audit.service.AuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorServiceTest {

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private DoctorMapper doctorMapper;

    @Mock
    private DoctorValidator validator;

    @Mock
    private AuditService auditService;

    @Mock
    private UserFacade userFacade;

    @Mock
    private SpecialtyFacade specialtyFacade;

    @InjectMocks
    private DoctorServiceImpl doctorService;

    private CreateDoctorRequest createRequest;
    private UpdateDoctorRequest updateRequest;
    private UpgradeToDoctorRequest upgradeRequest;
    private Doctor doctor;
    private User user;
    private Specialty specialty;
    private Role doctorRole;
    private CurrentUserDto currentUser;

    @BeforeEach
    void setUp() {
        doctorRole = new Role();
        doctorRole.setId(2L);
        doctorRole.setName(RoleConstants.DOCTOR);

        user = new User();
        user.setId(1L);
        user.setEmail("doctor@example.com");
        user.setFullName("John Doe");
        user.setRole(doctorRole);
        user.setIsActive(true);

        specialty = new Specialty();
        specialty.setId(1L);
        specialty.setName("Cardiology");

        doctor = new Doctor();
        doctor.setId(1L);
        doctor.setUser(user);
        doctor.setSpecialty(specialty);
        doctor.setDegree("MD");
        doctor.setExperienceYears(10);
        doctor.setConsultationFee(BigDecimal.valueOf(100));
        doctor.setBiography("Experienced cardiologist");
        doctor.setAverageRating(BigDecimal.valueOf(4.5));
        doctor.setTotalReviews(20);

        createRequest = new CreateDoctorRequest();
        createRequest.setUserId(1L);
        createRequest.setSpecialtyId(1L);
        createRequest.setDegree("MD");
        createRequest.setExperienceYears(10);
        createRequest.setConsultationFee(BigDecimal.valueOf(100));
        createRequest.setBiography("Experienced cardiologist");

        updateRequest = new UpdateDoctorRequest();
        updateRequest.setSpecialtyId(1L);
        updateRequest.setDegree("PhD");
        updateRequest.setExperienceYears(15);
        updateRequest.setConsultationFee(BigDecimal.valueOf(150));
        updateRequest.setBiography("Updated biography");

        upgradeRequest = new UpgradeToDoctorRequest();
        upgradeRequest.setUserId(1L);
        upgradeRequest.setSpecialtyId(1L);
        upgradeRequest.setDegree("MD");
        upgradeRequest.setExperienceYears(5);
        upgradeRequest.setConsultationFee(BigDecimal.valueOf(80));
        upgradeRequest.setBiography("New doctor");

        currentUser = new CurrentUserDto(1L, "DOCTOR");
    }

    @Test
    void createDoctor_Success() {
        when(userFacade.getUserById(1L)).thenReturn(user);
        when(specialtyFacade.getSpecialtyById(1L)).thenReturn(specialty);
        when(doctorRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(doctorMapper.toEntity(any(CreateDoctorRequest.class))).thenReturn(doctor);
        when(doctorRepository.save(any(Doctor.class))).thenReturn(doctor);
        when(doctorRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(doctor));
        when(doctorMapper.toResponse(any(Doctor.class))).thenReturn(new DoctorResponse());
        doNothing().when(auditService).log(anyString(), anyString(), anyLong(), any(), any());

        DoctorResponse response = doctorService.createDoctor(createRequest);

        assertThat(response).isNotNull();
        verify(doctorRepository).save(any(Doctor.class));
        verify(auditService).log(eq("CREATE"), eq("DOCTOR"), anyLong(), any(), any());
    }

    @Test
    void createDoctor_UserNotDoctorRole() {
        user.setRole(new Role(3L, "CUSTOMER"));
        when(userFacade.getUserById(1L)).thenReturn(user);

        assertThatThrownBy(() -> doctorService.createDoctor(createRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("User must have DOCTOR role");

        verify(doctorRepository, never()).save(any(Doctor.class));
    }

    @Test
    void createDoctor_UserInactive() {
        user.setIsActive(false);
        when(userFacade.getUserById(1L)).thenReturn(user);

        assertThatThrownBy(() -> doctorService.createDoctor(createRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("User is inactive");

        verify(doctorRepository, never()).save(any(Doctor.class));
    }

    @Test
    void createDoctor_ProfileAlreadyExists() {
        when(userFacade.getUserById(1L)).thenReturn(user);
        when(doctorRepository.findByUserId(1L)).thenReturn(Optional.of(doctor));

        assertThatThrownBy(() -> doctorService.createDoctor(createRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Doctor profile already exists");

        verify(doctorRepository, never()).save(any(Doctor.class));
    }

    @Test
    void createDoctor_RestoreSoftDeleted() {
        doctor.setDeletedAt(LocalDateTime.now());
        when(userFacade.getUserById(1L)).thenReturn(user);
        when(specialtyFacade.getSpecialtyById(1L)).thenReturn(specialty);
        when(doctorRepository.findByUserId(1L)).thenReturn(Optional.of(doctor));
        when(doctorRepository.save(any(Doctor.class))).thenReturn(doctor);
        when(doctorRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(doctor));
        when(doctorMapper.toResponse(any(Doctor.class))).thenReturn(new DoctorResponse());
        doNothing().when(auditService).log(anyString(), anyString(), anyLong(), any(), any());

        DoctorResponse response = doctorService.createDoctor(createRequest);

        assertThat(response).isNotNull();
        assertThat(doctor.getDeletedAt()).isNull();
        verify(doctorRepository).save(doctor);
        verify(auditService).log(eq("RESTORE"), eq("DOCTOR"), anyLong(), any(), any());
    }

    @Test
    void upgradeToDoctor_Success() {
        user.setRole(new Role(3L, "CUSTOMER"));
        when(userFacade.getUserById(1L)).thenReturn(user);
        doNothing().when(userFacade).upgradeToDoctor(1L);
        when(specialtyFacade.getSpecialtyById(1L)).thenReturn(specialty);
        when(doctorRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(doctorMapper.toEntity(any(UpgradeToDoctorRequest.class))).thenReturn(doctor);
        when(doctorRepository.save(any(Doctor.class))).thenReturn(doctor);
        when(doctorRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(doctor));
        when(doctorMapper.toResponse(any(Doctor.class))).thenReturn(new DoctorResponse());
        doNothing().when(auditService).log(anyString(), anyString(), anyLong(), any(), any());

        DoctorResponse response = doctorService.upgradeToDoctor(upgradeRequest);

        assertThat(response).isNotNull();
        verify(userFacade).upgradeToDoctor(1L);
        verify(doctorRepository).save(any(Doctor.class));
        verify(auditService).log(eq("UPGRADE"), eq("DOCTOR"), anyLong(), any(), any());
    }

    @Test
    void upgradeToDoctor_AlreadyDoctor() {
        when(userFacade.getUserById(1L)).thenReturn(user);

        assertThatThrownBy(() -> doctorService.upgradeToDoctor(upgradeRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("User is already a doctor");

        verify(userFacade, never()).upgradeToDoctor(anyLong());
    }

    @Test
    void upgradeToDoctor_UserInactive() {
        user.setRole(new Role(3L, "CUSTOMER"));
        user.setIsActive(false);
        when(userFacade.getUserById(1L)).thenReturn(user);

        assertThatThrownBy(() -> doctorService.upgradeToDoctor(upgradeRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("User is inactive");

        verify(userFacade, never()).upgradeToDoctor(anyLong());
    }

    @Test
    void getDoctorById_Success() {
        when(doctorRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(doctor));
        when(doctorMapper.toResponse(any(Doctor.class))).thenReturn(new DoctorResponse());

        DoctorResponse response = doctorService.getDoctorById(1L);

        assertThat(response).isNotNull();
        verify(doctorRepository).findByIdAndDeletedAtIsNull(1L);
    }

    @Test
    void getDoctorById_NotFound() {
        when(doctorRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorService.getDoctorById(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Doctor not found");
    }

    @Test
    void searchDoctors_Success() {
        DoctorSearchRequest request = new DoctorSearchRequest();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Doctor> doctorPage = new PageImpl<>(List.of(doctor));

        doNothing().when(validator).validateSearchRequest(any(DoctorSearchRequest.class));
        when(doctorRepository.findAll(ArgumentMatchers.<Specification<Doctor>>any(), any(Pageable.class)))
                .thenReturn(doctorPage);
        when(doctorMapper.toSummaryResponse(any(Doctor.class))).thenReturn(new DoctorSummaryResponse());

        PageResponse<DoctorSummaryResponse> result = doctorService.searchDoctors(request, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getItems()).hasSize(1);
        verify(doctorRepository).findAll(ArgumentMatchers.<Specification<Doctor>>any(), any(Pageable.class));
    }

    @Test
    void updateDoctor_Success() {
        when(userFacade.getCurrentUser()).thenReturn(currentUser);
        when(doctorRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(doctor));
        when(doctorMapper.toSnapshot(any(Doctor.class))).thenReturn(doctor);
        doNothing().when(doctorMapper).updateEntity(any(UpdateDoctorRequest.class), any(Doctor.class));
        when(specialtyFacade.getSpecialtyById(1L)).thenReturn(specialty);
        when(doctorRepository.save(any(Doctor.class))).thenReturn(doctor);
        when(doctorRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(doctor));
        when(doctorMapper.toResponse(any(Doctor.class))).thenReturn(new DoctorResponse());
        doNothing().when(auditService).log(anyString(), anyString(), anyLong(), any(), any());

        DoctorResponse response = doctorService.updateDoctor(1L, updateRequest);

        assertThat(response).isNotNull();
        verify(doctorRepository).save(doctor);
        verify(auditService).log(eq("UPDATE"), eq("DOCTOR"), anyLong(), any(), any());
    }

    @Test
    void updateDoctor_AccessDenied() {
        CurrentUserDto otherUser = new CurrentUserDto(2L, "DOCTOR");
        when(userFacade.getCurrentUser()).thenReturn(otherUser);
        when(doctorRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(doctor));

        assertThatThrownBy(() -> doctorService.updateDoctor(1L, updateRequest))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("not allowed to update this doctor profile");

        verify(doctorRepository, never()).save(any(Doctor.class));
    }

    @Test
    void updateDoctor_NotFound() {
        when(userFacade.getCurrentUser()).thenReturn(currentUser);
        when(doctorRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorService.updateDoctor(1L, updateRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Doctor not found");

        verify(doctorRepository, never()).save(any(Doctor.class));
    }

    @Test
    void deleteDoctor_Success() {
        when(doctorRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(doctor));
        when(doctorMapper.toSnapshot(any(Doctor.class))).thenReturn(doctor);
        when(doctorRepository.save(any(Doctor.class))).thenReturn(doctor);
        doNothing().when(auditService).log(anyString(), anyString(), anyLong(), any(), any());

        doctorService.deleteDoctor(1L);

        assertThat(doctor.getDeletedAt()).isNotNull();
        verify(doctorRepository).save(doctor);
        verify(auditService).log(eq("DELETE"), eq("DOCTOR"), anyLong(), any(), any());
    }

    @Test
    void deleteDoctor_NotFound() {
        when(doctorRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorService.deleteDoctor(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Doctor not found");

        verify(doctorRepository, never()).save(any(Doctor.class));
    }

    @Test
    void getDoctorByUserId_Success() {
        when(doctorRepository.findByUserIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(doctor));

        Doctor result = doctorService.getDoctorByUserId(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(doctorRepository).findByUserIdAndDeletedAtIsNull(1L);
    }

    @Test
    void getDoctorByUserId_NotFound() {
        when(doctorRepository.findByUserIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorService.getDoctorByUserId(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Doctor not found with this account");
    }

    @Test
    void exists_True() {
        when(doctorRepository.existsById(1L)).thenReturn(true);

        boolean result = doctorService.exists(1L);

        assertThat(result).isTrue();
    }

    @Test
    void exists_False() {
        when(doctorRepository.existsById(1L)).thenReturn(false);

        boolean result = doctorService.exists(1L);

        assertThat(result).isFalse();
    }
}
