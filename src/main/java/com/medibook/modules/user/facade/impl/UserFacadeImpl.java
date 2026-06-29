package com.medibook.modules.user.facade.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.medibook.common.exception.ResourceNotFoundException;
import com.medibook.modules.user.dto.internal.CurrentUserDto;
import com.medibook.modules.user.dto.internal.CurrentUserResponse;
import com.medibook.modules.user.entity.Role;
import com.medibook.modules.user.entity.User;
import com.medibook.modules.user.facade.UserFacade;
import com.medibook.modules.user.repository.UserRepository;
import com.medibook.modules.user.service.RoleService;
import com.medibook.modules.user.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserFacadeImpl implements UserFacade {

    private final UserRepository userRepository;
    private final RoleService roleService;
    private final UserService userService;

    @Override
    public User getUserById(Long id) {

        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));

    }

    @Override
    @Transactional
    public void upgradeToDoctor(Long userId) {

        User user = getUserById(userId);

        Role doctorRole = roleService.getDoctorRole();

        user.setRole(doctorRole);

        userRepository.save(user);
    }

    @Override
    public CurrentUserDto getCurrentUser() {

        User user = userService.getCurrentUser();

        return CurrentUserDto.builder().id(user.getId()).role(user.getRole().getName()).build();
    }

    @Override
    public CurrentUserResponse getCurrentUserAndName() {

        User user = userService.getCurrentUser();

        return CurrentUserResponse.builder().id(user.getId()).fullName(user.getFullName()).build();
    }
}
