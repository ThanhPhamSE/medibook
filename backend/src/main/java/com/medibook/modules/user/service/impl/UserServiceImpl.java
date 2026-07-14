package com.medibook.modules.user.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.medibook.common.exception.ResourceNotFoundException;
import com.medibook.common.response.PageResponse;
import com.medibook.common.response.util.PageMapper;
import com.medibook.modules.user.dto.request.UpdateProfileRequest;
import com.medibook.modules.user.dto.request.UserSearchRequest;
import com.medibook.modules.user.dto.response.UserResponse;
import com.medibook.modules.user.entity.User;
import com.medibook.modules.user.mapper.UserMapper;
import com.medibook.modules.user.repository.UserRepository;
import com.medibook.modules.user.service.UserService;
import com.medibook.modules.user.specification.UserSpecification;
import com.medibook.modules.doctor.repository.DoctorRepository;
import com.medibook.common.constant.RoleConstants;
import com.medibook.security.context.CurrentUserRequestCache;
import com.medibook.security.util.SecurityUtils;
import com.medibook.modules.audit.service.AuditService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

        private final UserRepository userRepository;
        private final UserMapper userMapper;
        private final DoctorRepository doctorRepository;
        private final CurrentUserRequestCache currentUserRequestCache;
        private final AuditService auditService;

        @Override
        @Transactional(readOnly = true)
        public User getCurrentUser() {

                return currentUserRequestCache.getUser()
                                .orElseGet(() -> userRepository.findById(SecurityUtils.getCurrentUserId())
                                                .orElseThrow(() -> new ResourceNotFoundException("User not found")));
        }

        @Override
        @Transactional(readOnly = true)
        public PageResponse<UserResponse> getAllUsers(UserSearchRequest request, Pageable pageable) {

                Specification<User> spec = UserSpecification.notDeleted()
                                .and(UserSpecification.keyword(request.getKeyword()))
                                .and(UserSpecification.roleId(request.getRoleId()))
                                .and(UserSpecification.roleName(request.getRole()))
                                .and(UserSpecification.isActive(request.getIsActive()));


                Page<UserResponse> page = userRepository.findAll(spec, pageable)
                                .map(userMapper::toResponse);

                return PageMapper.from(page);
        }

        @Override
        @Transactional(readOnly = true)
        public UserResponse getUserById(Long id) {

                User user = userRepository.findByIdAndDeletedAtIsNull(id)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                UserResponse response = userMapper.toResponse(user);
                if (user.getRole() != null && RoleConstants.DOCTOR.equals(user.getRole().getName())) {
                        doctorRepository.findByUserIdAndDeletedAtIsNull(id)
                                        .ifPresent(doctor -> response.setDoctorId(doctor.getId()));
                }
                return response;
        }

        @Override
        @Transactional
        public void activateUser(Long id) {
                log.info("Request to activate user: id={}", id);

                User user = userRepository.findByIdAndDeletedAtIsNull(id)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                user.setIsActive(true);
                userRepository.save(user);

                auditService.log("ACTIVATE", "User", user.getId(), false, true);

                log.info("Successfully activated user: id={}", id);
        }

        @Override
        @Transactional
        public void deactivateUser(Long id) {
                log.info("Request to deactivate user: id={}", id);

                User user = userRepository.findByIdAndDeletedAtIsNull(id)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                user.setIsActive(false);
                userRepository.save(user);

                auditService.log("DEACTIVATE", "User", user.getId(), true, false);

                log.info("Successfully deactivated user: id={}", id);
        }

        @Override
        @Transactional
        public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
                log.info("Request to update profile: userId={}", userId);

                User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                userMapper.updateUserFromRequest(request, user);
                userRepository.save(user);

                auditService.log("UPDATE", "User", user.getId(), null, user);

                log.info("Successfully updated profile: userId={}", userId);

                UserResponse response = userMapper.toResponse(user);
                if (user.getRole() != null && RoleConstants.DOCTOR.equals(user.getRole().getName())) {
                        doctorRepository.findByUserIdAndDeletedAtIsNull(userId)
                                        .ifPresent(doctor -> response.setDoctorId(doctor.getId()));
                }
                return response;
        }
}