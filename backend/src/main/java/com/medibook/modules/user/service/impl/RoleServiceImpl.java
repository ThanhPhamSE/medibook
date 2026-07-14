package com.medibook.modules.user.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.medibook.common.exception.ResourceNotFoundException;
import com.medibook.modules.user.dto.response.RoleInfoResponse;
import com.medibook.modules.user.entity.Role;
import com.medibook.modules.user.repository.RoleRepository;
import com.medibook.modules.user.repository.UserRepository;
import com.medibook.modules.user.service.RoleService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    @Override
    public Role getDoctorRole() {

        return roleRepository.findByName("DOCTOR")
                .orElseThrow(() -> new ResourceNotFoundException("DOCTOR role not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleInfoResponse> getAllRoles() {
        List<Role> roles = roleRepository.findAll();
        return roles.stream().map(role -> {
            long usersCount = userRepository.countByRoleIdAndDeletedAtIsNull(role.getId());
            int permsCount = getPermsCount(role.getName());
            String description = getRoleDescription(role.getName());
            return RoleInfoResponse.builder()
                    .id(role.getId())
                    .name(role.getName())
                    .description(description)
                    .usersCount(usersCount)
                    .permissions(permsCount)
                    .build();
        }).collect(Collectors.toList());
    }

    private int getPermsCount(String name) {
        switch (name.toUpperCase()) {
            case "ADMIN": return 9;
            case "DOCTOR": return 5;
            case "CUSTOMER": return 3;
            default: return 0;
        }
    }

    private String getRoleDescription(String name) {
        switch (name.toUpperCase()) {
            case "ADMIN": return "Full system access";
            case "DOCTOR": return "Manage appointments and patients";
            case "CUSTOMER": return "Book appointments and view records";
            default: return "Access level";
        }
    }

}

