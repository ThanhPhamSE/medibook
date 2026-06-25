package com.medibook.modules.user.facade.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.medibook.common.exception.ResourceNotFoundException;
import com.medibook.modules.user.entity.Role;
import com.medibook.modules.user.entity.User;
import com.medibook.modules.user.facade.UserFacade;
import com.medibook.modules.user.repository.UserRepository;
import com.medibook.modules.user.service.RoleService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserFacadeImpl implements UserFacade {

    private final UserRepository userRepository;
    private final RoleService roleService;

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

}
