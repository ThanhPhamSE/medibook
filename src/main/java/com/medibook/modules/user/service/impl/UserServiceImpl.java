package com.medibook.modules.user.service.impl;

import com.medibook.modules.user.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.medibook.common.exception.ResourceNotFoundException;
import com.medibook.modules.user.entity.User;
import com.medibook.modules.user.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public User getCurrentUser() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        return userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    }

}
