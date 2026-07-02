package com.medibook.modules.user.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.medibook.common.exception.ResourceNotFoundException;
import com.medibook.common.response.PageResponse;
import com.medibook.common.response.util.PageMapper;
import com.medibook.modules.user.dto.request.UserSearchRequest;
import com.medibook.modules.user.dto.response.UserResponse;
import com.medibook.modules.user.entity.User;
import com.medibook.modules.user.mapper.UserMapper;
import com.medibook.modules.user.repository.UserRepository;
import com.medibook.modules.user.service.UserService;
import com.medibook.modules.user.specification.UserSpecification;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

        private final UserRepository userRepository;
        private final UserMapper userMapper;

        @Override
        @Transactional(readOnly = true)
        public User getCurrentUser() {

                String email = org.springframework.security.core.context.SecurityContextHolder
                                .getContext()
                                .getAuthentication()
                                .getName();

                return userRepository.findByEmailAndDeletedAtIsNull(email)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        }

        @Override
        @Transactional(readOnly = true)
        public PageResponse<UserResponse> getAllUsers(UserSearchRequest request, Pageable pageable) {

                Specification<User> spec = UserSpecification.notDeleted()
                                .and(UserSpecification.keyword(request.getKeyword()))
                                .and(UserSpecification.roleId(request.getRoleId()))
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

                return userMapper.toResponse(user);
        }

        @Override
        @Transactional
        public void activateUser(Long id) {

                User user = userRepository.findByIdAndDeletedAtIsNull(id)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                user.setIsActive(true);
                userRepository.save(user);
        }

        @Override
        @Transactional
        public void deactivateUser(Long id) {

                User user = userRepository.findByIdAndDeletedAtIsNull(id)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                user.setIsActive(false);
                userRepository.save(user);
        }
}