package com.medibook.modules.auth.service.impl;

import com.medibook.modules.auth.validator.AuthValidator;

import java.time.ZoneId;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.medibook.common.exception.ResourceNotFoundException;
import com.medibook.common.exception.UnauthorizedException;
import com.medibook.modules.auth.dto.request.LoginRequest;
import com.medibook.modules.auth.dto.request.RegisterRequest;
import com.medibook.modules.auth.dto.response.LoginResponse;
import com.medibook.modules.auth.dto.response.RegisterResponse;
import com.medibook.modules.auth.mapper.AuthMapper;
import com.medibook.modules.auth.service.AuthService;
import com.medibook.modules.token.entity.RefreshToken;
import com.medibook.modules.token.service.RefreshTokenService;
import com.medibook.modules.user.entity.Role;
import com.medibook.modules.user.entity.User;
import com.medibook.modules.user.repository.RoleRepository;
import com.medibook.modules.user.repository.UserRepository;
import com.medibook.security.jwt.JwtProperties;
import com.medibook.security.jwt.JwtService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthValidator authValidator;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final JwtProperties jwtProperties;
    private final AuthMapper authMapper;

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {

        authValidator.validateRegister(request);

        Role customRole = roleRepository.findByName("CUSTOMER")
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = authMapper.toUser(request, encodedPassword, customRole);

        userRepository.save(user);

        return authMapper.toRegisterResponse(user);
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {

        authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        long issuedAt = System.currentTimeMillis();

        String accessToken = jwtService.generateToken(user.getId(), user.getEmail(), user.getPassword());

        String refreshToken = refreshTokenService.createRefreshToken(user, "WEB", "127.0.0.1");

        long accessExp = issuedAt + jwtProperties.getAccessTokenExpiration();

        long refreshExp = issuedAt + jwtProperties.getRefreshTokenExpiration();

        return authMapper.toLoginResponse(user, accessToken, refreshToken, accessExp, refreshExp, issuedAt);
    }

    @Override
    @Transactional
    public LoginResponse refreshToken(String rawRefreshToken) {

        RefreshToken refreshToken = refreshTokenService.verifyToken(rawRefreshToken);

        User user = refreshToken.getUser();

        long issueAt = System.currentTimeMillis();

        String accessToken = jwtService.generateToken(user.getId(), user.getEmail(), user.getPassword());

        long accessExp = issueAt + jwtProperties.getAccessTokenExpiration();

        long refreshExp = refreshToken.getExpiresAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        return authMapper.toLoginResponse(user, accessToken, rawRefreshToken, accessExp, refreshExp, issueAt);
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        refreshTokenService.revokeToken(refreshToken);
    }
}
