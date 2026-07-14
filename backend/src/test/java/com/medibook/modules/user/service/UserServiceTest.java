package com.medibook.modules.user.service;

import com.medibook.common.exception.ResourceNotFoundException;
import com.medibook.common.response.PageResponse;
import com.medibook.modules.doctor.repository.DoctorRepository;
import com.medibook.modules.user.dto.request.UpdateProfileRequest;
import com.medibook.modules.user.dto.request.UserSearchRequest;
import com.medibook.modules.user.dto.response.UserResponse;
import com.medibook.modules.user.entity.User;
import com.medibook.modules.user.mapper.UserMapper;
import com.medibook.modules.user.repository.UserRepository;
import com.medibook.modules.user.service.impl.UserServiceImpl;
import com.medibook.modules.audit.service.AuditService;
import com.medibook.security.context.CurrentUserRequestCache;
import com.medibook.security.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private CurrentUserRequestCache currentUserRequestCache;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setFullName("John Doe");
        user.setIsActive(true);

        userResponse = new UserResponse();
        userResponse.setId(1L);
        userResponse.setEmail("test@example.com");
        userResponse.setFullName("John Doe");
    }

    @Test
    void getCurrentUser_Success() {
        when(currentUserRequestCache.getUser()).thenReturn(Optional.of(user));

        User result = userService.getCurrentUser();

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(currentUserRequestCache).getUser();
    }

    @Test
    void getCurrentUser_FromRepository() {
        when(currentUserRequestCache.getUser()).thenReturn(Optional.empty());
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(1L);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));

            User result = userService.getCurrentUser();

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            verify(userRepository).findById(1L);
        }
    }

    @Test
    void getAllUsers_Success() {
        UserSearchRequest request = new UserSearchRequest();
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(user));

        when(userRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), eq(pageable))).thenReturn(userPage);
        when(userMapper.toResponse(any(User.class))).thenReturn(userResponse);

        PageResponse<UserResponse> result = userService.getAllUsers(request, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getItems()).hasSize(1);
        verify(userRepository).findAll(any(org.springframework.data.jpa.domain.Specification.class), eq(pageable));
    }

    @Test
    void getUserById_Success() {
        when(userRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(user));
        when(userMapper.toResponse(any(User.class))).thenReturn(userResponse);

        UserResponse result = userService.getUserById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(userRepository).findByIdAndDeletedAtIsNull(1L);
    }

    @Test
    void getUserById_NotFound() {
        when(userRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void activateUser_Success() {
        user.setIsActive(false);
        when(userRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        doNothing().when(auditService).log(anyString(), anyString(), anyLong(), any(), any());

        userService.activateUser(1L);

        assertThat(user.getIsActive()).isTrue();
        verify(userRepository).save(user);
        verify(auditService).log(eq("ACTIVATE"), eq("User"), eq(1L), eq(false), eq(true));
    }

    @Test
    void activateUser_NotFound() {
        when(userRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.activateUser(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deactivateUser_Success() {
        user.setIsActive(true);
        when(userRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        doNothing().when(auditService).log(anyString(), anyString(), anyLong(), any(), any());

        userService.deactivateUser(1L);

        assertThat(user.getIsActive()).isFalse();
        verify(userRepository).save(user);
        verify(auditService).log(eq("DEACTIVATE"), eq("User"), eq(1L), eq(true), eq(false));
    }

    @Test
    void deactivateUser_NotFound() {
        when(userRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deactivateUser(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateProfile_Success() {
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFullName("Jane Smith");

        when(userRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(user));
        doNothing().when(userMapper).updateUserFromRequest(any(UpdateProfileRequest.class), any(User.class));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toResponse(any(User.class))).thenReturn(userResponse);
        doNothing().when(auditService).log(anyString(), anyString(), anyLong(), any(), any());

        UserResponse result = userService.updateProfile(1L, request);

        assertThat(result).isNotNull();
        verify(userRepository).save(user);
        verify(auditService).log(eq("UPDATE"), eq("User"), eq(1L), any(), any());
    }

    @Test
    void updateProfile_NotFound() {
        UpdateProfileRequest request = new UpdateProfileRequest();
        when(userRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateProfile(1L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(userRepository, never()).save(any(User.class));
    }
}
