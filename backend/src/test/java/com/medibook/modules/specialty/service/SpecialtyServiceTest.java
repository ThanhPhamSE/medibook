package com.medibook.modules.specialty.service;

import com.medibook.common.exception.BadRequestException;
import com.medibook.common.exception.ConflictException;
import com.medibook.common.exception.ResourceNotFoundException;
import com.medibook.common.response.PageResponse;
import com.medibook.modules.specialty.dto.request.SpecialtyCreateRequest;
import com.medibook.modules.specialty.dto.request.SpecialtyUpdateRequest;
import com.medibook.modules.specialty.dto.response.SpecialtyResponse;
import com.medibook.modules.specialty.entity.Specialty;
import com.medibook.modules.specialty.mapper.SpecialtyMapper;
import com.medibook.modules.specialty.repository.SpecialtyRepository;
import com.medibook.modules.specialty.service.impl.SpecialtyServiceImpl;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpecialtyServiceTest {

    @Mock
    private SpecialtyRepository specialtyRepository;

    @Mock
    private SpecialtyMapper specialtyMapper;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private SpecialtyServiceImpl specialtyService;

    private SpecialtyCreateRequest createRequest;
    private SpecialtyUpdateRequest updateRequest;
    private Specialty specialty;

    @BeforeEach
    void setUp() {
        createRequest = new SpecialtyCreateRequest();
        createRequest.setName("Cardiology");
        createRequest.setDescription("Heart and blood vessel specialists");

        updateRequest = new SpecialtyUpdateRequest();
        updateRequest.setName("Updated Cardiology");
        updateRequest.setDescription("Updated description");

        specialty = new Specialty();
        specialty.setId(1L);
        specialty.setName("Cardiology");
        specialty.setDescription("Heart and blood vessel specialists");
    }

    @Test
    void create_Success() {
        when(specialtyRepository.findForUpdateByName("Cardiology")).thenReturn(Optional.empty());
        when(specialtyMapper.toEntity(any(SpecialtyCreateRequest.class))).thenReturn(specialty);
        when(specialtyRepository.save(any(Specialty.class))).thenReturn(specialty);
        when(specialtyMapper.toResponse(any(Specialty.class))).thenReturn(new SpecialtyResponse());
        doNothing().when(auditService).log(anyString(), anyString(), anyLong(), any(), any());

        SpecialtyResponse response = specialtyService.create(createRequest);

        assertThat(response).isNotNull();
        verify(specialtyRepository).save(any(Specialty.class));
        verify(auditService).log(eq("CREATE"), eq("SPECIALTY"), anyLong(), any(), any());
    }

    @Test
    void create_AlreadyExists() {
        when(specialtyRepository.findForUpdateByName("Cardiology")).thenReturn(Optional.of(specialty));

        assertThatThrownBy(() -> specialtyService.create(createRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Specialty already exists");

        verify(specialtyRepository, never()).save(any(Specialty.class));
    }

    @Test
    void create_RestoreSoftDeleted() {
        specialty.setDeletedAt(LocalDateTime.now());
        when(specialtyRepository.findForUpdateByName("Cardiology")).thenReturn(Optional.of(specialty));
        when(specialtyRepository.save(any(Specialty.class))).thenReturn(specialty);
        when(specialtyMapper.toResponse(any(Specialty.class))).thenReturn(new SpecialtyResponse());
        doNothing().when(auditService).log(anyString(), anyString(), anyLong(), any(), any());

        SpecialtyResponse response = specialtyService.create(createRequest);

        assertThat(response).isNotNull();
        assertThat(specialty.getDeletedAt()).isNull();
        verify(specialtyRepository).save(specialty);
        verify(auditService).log(eq("RESTORE"), eq("SPECIALTY"), anyLong(), any(), any());
    }

    @Test
    void update_Success() {
        when(specialtyRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(specialty));
        when(specialtyRepository.findForUpdateByName("Updated Cardiology")).thenReturn(Optional.empty());
        when(specialtyMapper.toSnapshot(any(Specialty.class))).thenReturn(specialty);
        doNothing().when(specialtyMapper).updateEntity(any(SpecialtyUpdateRequest.class), any(Specialty.class));
        when(specialtyRepository.save(any(Specialty.class))).thenReturn(specialty);
        when(specialtyMapper.toResponse(any(Specialty.class))).thenReturn(new SpecialtyResponse());
        doNothing().when(auditService).log(anyString(), anyString(), anyLong(), any(), any());

        SpecialtyResponse response = specialtyService.update(1L, updateRequest);

        assertThat(response).isNotNull();
        verify(specialtyRepository).save(specialty);
        verify(auditService).log(eq("UPDATE"), eq("SPECIALTY"), anyLong(), any(), any());
    }

    @Test
    void update_NotFound() {
        when(specialtyRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> specialtyService.update(1L, updateRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Specialty not found");

        verify(specialtyRepository, never()).save(any(Specialty.class));
    }

    @Test
    void update_NameConflict() {
        Specialty otherSpecialty = new Specialty();
        otherSpecialty.setId(2L);
        otherSpecialty.setName("Updated Cardiology");

        when(specialtyRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(specialty));
        when(specialtyRepository.findForUpdateByName("Updated Cardiology")).thenReturn(Optional.of(otherSpecialty));

        assertThatThrownBy(() -> specialtyService.update(1L, updateRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Specialty already exists");

        verify(specialtyRepository, never()).save(any(Specialty.class));
    }

    @Test
    void getById_Success() {
        when(specialtyRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(specialty));
        when(specialtyMapper.toResponse(any(Specialty.class))).thenReturn(new SpecialtyResponse());

        SpecialtyResponse response = specialtyService.getById(1L);

        assertThat(response).isNotNull();
        verify(specialtyRepository).findByIdAndDeletedAtIsNull(1L);
    }

    @Test
    void getById_NotFound() {
        when(specialtyRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> specialtyService.getById(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Specialty not found");
    }

    @Test
    void delete_Success() {
        when(specialtyRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(specialty));
        when(specialtyRepository.save(any(Specialty.class))).thenReturn(specialty);
        doNothing().when(auditService).log(anyString(), anyString(), anyLong(), any(), any());

        specialtyService.delete(1L);

        assertThat(specialty.getDeletedAt()).isNotNull();
        verify(specialtyRepository).save(specialty);
        verify(auditService).log(eq("DELETE"), eq("SPECIALTY"), anyLong(), any(), any());
    }

    @Test
    void delete_NotFound() {
        when(specialtyRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> specialtyService.delete(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Specialty not found");

        verify(specialtyRepository, never()).save(any(Specialty.class));
    }

    @Test
    void getAllByNameAndPage_WithKeyword() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Specialty> specialtyPage = new PageImpl<>(List.of(specialty));

        when(specialtyRepository.findByNameContainingIgnoreCaseAndDeletedAtIsNull("Cardio", pageable)).thenReturn(specialtyPage);
        when(specialtyMapper.toResponse(any(Specialty.class))).thenReturn(new SpecialtyResponse());

        PageResponse<SpecialtyResponse> response = specialtyService.getAllByNameAndPage("Cardio", pageable);

        assertThat(response).isNotNull();
        assertThat(response.getItems()).hasSize(1);
        verify(specialtyRepository).findByNameContainingIgnoreCaseAndDeletedAtIsNull("Cardio", pageable);
    }

    @Test
    void getAllByNameAndPage_WithoutKeyword() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Specialty> specialtyPage = new PageImpl<>(List.of(specialty));

        when(specialtyRepository.findByDeletedAtIsNull(pageable)).thenReturn(specialtyPage);
        when(specialtyMapper.toResponse(any(Specialty.class))).thenReturn(new SpecialtyResponse());

        PageResponse<SpecialtyResponse> response = specialtyService.getAllByNameAndPage(null, pageable);

        assertThat(response).isNotNull();
        assertThat(response.getItems()).hasSize(1);
        verify(specialtyRepository).findByDeletedAtIsNull(pageable);
    }

    @Test
    void restore_Success() {
        specialty.setDeletedAt(LocalDateTime.now());
        when(specialtyRepository.findByIdAndDeletedAtIsNotNull(1L)).thenReturn(Optional.of(specialty));
        when(specialtyRepository.findForUpdateByName("Cardiology")).thenReturn(Optional.empty());
        when(specialtyRepository.save(any(Specialty.class))).thenReturn(specialty);
        when(specialtyMapper.toResponse(any(Specialty.class))).thenReturn(new SpecialtyResponse());
        doNothing().when(auditService).log(anyString(), anyString(), anyLong(), any(), any());

        SpecialtyResponse response = specialtyService.restore(1L);

        assertThat(response).isNotNull();
        assertThat(specialty.getDeletedAt()).isNull();
        verify(specialtyRepository).save(specialty);
        verify(auditService).log(eq("RESTORE"), eq("SPECIALTY"), anyLong(), any(), any());
    }

    @Test
    void restore_NotFound() {
        when(specialtyRepository.findByIdAndDeletedAtIsNotNull(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> specialtyService.restore(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Deleted specialty not found");

        verify(specialtyRepository, never()).save(any(Specialty.class));
    }

    @Test
    void restore_NameConflict() {
        specialty.setDeletedAt(LocalDateTime.now());
        Specialty activeSpecialty = new Specialty();
        activeSpecialty.setId(2L);
        activeSpecialty.setName("Cardiology");

        when(specialtyRepository.findByIdAndDeletedAtIsNotNull(1L)).thenReturn(Optional.of(specialty));
        when(specialtyRepository.findForUpdateByName("Cardiology")).thenReturn(Optional.of(activeSpecialty));

        assertThatThrownBy(() -> specialtyService.restore(1L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Active specialty with same name exists");

        verify(specialtyRepository, never()).save(any(Specialty.class));
    }

    @Test
    void getDeleted_Success() {
        specialty.setDeletedAt(LocalDateTime.now());
        Pageable pageable = PageRequest.of(0, 10);
        Page<Specialty> specialtyPage = new PageImpl<>(List.of(specialty));

        when(specialtyRepository.findByDeletedAtIsNotNull(pageable)).thenReturn(specialtyPage);
        when(specialtyMapper.toResponse(any(Specialty.class))).thenReturn(new SpecialtyResponse());

        PageResponse<SpecialtyResponse> response = specialtyService.getDeleted(pageable);

        assertThat(response).isNotNull();
        assertThat(response.getItems()).hasSize(1);
        verify(specialtyRepository).findByDeletedAtIsNotNull(pageable);
    }

    @Test
    void getEntityById_Success() {
        when(specialtyRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(specialty));

        Specialty result = specialtyService.getEntityById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getEntityById_InvalidId() {
        assertThatThrownBy(() -> specialtyService.getEntityById(-1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("ID must be a positive number");
    }

    @Test
    void getEntityById_NullId() {
        assertThatThrownBy(() -> specialtyService.getEntityById(null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("ID must be a positive number");
    }
}
