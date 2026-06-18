package com.medibook.modules.user.service.impl;

import org.springframework.stereotype.Service;

import com.medibook.common.exception.ResourceNotFoundException;
import com.medibook.modules.user.entity.Role;
import com.medibook.modules.user.repository.RoleRepository;
import com.medibook.modules.user.service.RoleService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    public Role getDoctorRole() {

        return roleRepository.findByName("DOCTOR")
                .orElseThrow(() -> new ResourceNotFoundException("DOCTOR role not found"));
    }

}
