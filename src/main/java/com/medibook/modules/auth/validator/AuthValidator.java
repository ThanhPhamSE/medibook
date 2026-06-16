package com.medibook.modules.auth.validator;

import org.springframework.stereotype.Component;

import com.medibook.common.exception.ConflictException;
import com.medibook.modules.auth.dto.request.RegisterRequest;
import com.medibook.modules.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthValidator {

    private final UserRepository userRepository;

    public void validateRegister(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already exists");
        }

        if (userRepository.existsByPhone(request.getPhone())) {
            throw new ConflictException("Phone already exists");
        }
    }
}
