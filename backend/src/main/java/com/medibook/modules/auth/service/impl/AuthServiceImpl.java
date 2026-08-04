package com.medibook.modules.auth.service.impl;

import com.medibook.modules.auth.validator.AuthValidator;
import com.medibook.modules.notification.service.EmailService;

import java.time.ZoneId;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.medibook.common.constant.RoleConstants;
import com.medibook.common.exception.BadRequestException;
import com.medibook.common.exception.ResourceNotFoundException;
import com.medibook.common.exception.UnauthorizedException;
import com.medibook.modules.doctor.repository.DoctorRepository;
import com.medibook.modules.auth.dto.request.ChangePasswordRequest;
import com.medibook.modules.auth.dto.request.ForgotPasswordRequest;
import com.medibook.modules.auth.dto.request.LoginRequest;
import com.medibook.modules.auth.dto.request.RegisterRequest;
import com.medibook.modules.auth.dto.request.ResetPasswordRequest;
import com.medibook.modules.auth.dto.response.ChangePasswordResponse;
import com.medibook.modules.auth.dto.response.ForgotPasswordResponse;
import com.medibook.modules.auth.dto.response.LoginResponse;
import com.medibook.modules.auth.dto.response.RegisterResponse;
import com.medibook.modules.auth.dto.response.ResetPasswordResponse;
import com.medibook.modules.auth.mapper.AuthMapper;
import com.medibook.modules.auth.service.AuthService;
import com.medibook.modules.token.entity.RefreshToken;
import com.medibook.modules.token.service.RefreshTokenService;
import com.medibook.modules.user.dto.response.UserResponse;
import com.medibook.modules.user.entity.Role;
import com.medibook.modules.user.entity.User;
import com.medibook.modules.user.repository.RoleRepository;
import com.medibook.modules.user.repository.UserRepository;
import com.medibook.security.context.CurrentUserRequestCache;
import com.medibook.security.jwt.JwtProperties;
import com.medibook.security.jwt.JwtService;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
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
    private final EmailService emailService;
    private final DoctorRepository doctorRepository;
    private final CurrentUserRequestCache currentUserRequestCache;

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {

        authValidator.validateRegister(request);

        Role customRole = roleRepository.findByName("CUSTOMER")
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = authMapper.toUser(request, encodedPassword, customRole);

        user.setIsActive(false);

        userRepository.save(user);

        String token = jwtService.generateEmailVerificationToken(user.getId(), user.getEmail());

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    emailService.sendVerificationEmail(user.getEmail(), token);
                }
            });
        } else {
            emailService.sendVerificationEmail(user.getEmail(), token);
        }

        return authMapper.toRegisterResponse(user);
    }

    @Override
    public void resendVerificationEmail(String email) {

        if (email == null || email.isBlank()) {
            throw new BadRequestException("Email is required");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (Boolean.TRUE.equals(user.getIsActive())) {
            throw new BadRequestException("Email already verified");
        }

        String token = jwtService.generateEmailVerificationToken(user.getId(), user.getEmail());

        emailService.sendVerificationEmail(user.getEmail(), token);
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {

        try {

            authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        } catch (org.springframework.security.authentication.DisabledException ex) {

            throw new UnauthorizedException("Tài khoản chưa được kích hoạt. Vui lòng xác thực email của bạn trước khi đăng nhập.");

        } catch (AuthenticationException ex) {

            throw new UnauthorizedException("Invalid email or password");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        if (!user.getIsActive()) {
            throw new UnauthorizedException("Please verify your email before login");
        }

        long issuedAt = System.currentTimeMillis();

        String accessToken = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole().getName());

        String refreshToken = refreshTokenService.createRefreshToken(user, "WEB", "127.0.0.1");

        return authMapper.toLoginResponse(
                user,
                accessToken,
                refreshToken,
                issuedAt + jwtProperties.getAccessTokenExpiration(),
                issuedAt + jwtProperties.getRefreshTokenExpiration(),
                issuedAt);
    }

    @Override
    @Transactional
    public LoginResponse refreshToken(String rawRefreshToken) {

        RefreshToken refreshToken = refreshTokenService.verifyToken(rawRefreshToken);

        User user = refreshToken.getUser();

        long issueAt = System.currentTimeMillis();

        String accessToken = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole().getName());

        long accessExp = issueAt + jwtProperties.getAccessTokenExpiration();

        long refreshExp = refreshToken.getExpiresAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        return authMapper.toLoginResponse(user, accessToken, rawRefreshToken, accessExp, refreshExp, issueAt);
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {

        refreshTokenService.revokeToken(refreshToken);
    }

    @Override
    @Transactional
    public void logoutAllDevices(Long userId) {

        refreshTokenService.revokeAllUserTokens(userId);
    }

    @Override
    public ChangePasswordResponse changePassword(Long userId, ChangePasswordRequest request) {

        authValidator.validateChangePassword(request);

        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean matched = passwordEncoder.matches(request.getCurrentPassword(), user.getPassword());

        if (!matched) {
            throw new UnauthorizedException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        userRepository.save(user);

        refreshTokenService.revokeAllUserTokens(userId);

        return ChangePasswordResponse.builder().userId(user.getId()).email(user.getEmail())
                .message("Password changed successfully. Please login again").build();
    }

    @Override
    public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request) {

        request.normalize();

        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {

            String token = jwtService.generatePasswordResetToken(user.getId(), user.getEmail());

            emailService.sendResetPasswordEmail(user.getEmail(), token);
        });

        return ForgotPasswordResponse.builder().message("If an account exists, a password reset email been sent")
                .build();

    }

    @Override
    public ResetPasswordResponse resetPassword(ResetPasswordRequest request) {

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Password confirmation does not match");
        }

        Long userId = jwtService.extractUserIdFromResetToken(request.getResetToken());

        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        userRepository.save(user);

        refreshTokenService.revokeAllUserTokens(userId);

        return ResetPasswordResponse.builder().userId(user.getId()).email(user.getEmail())
                .message("Password reset successful").build();

    }

    @Transactional
    public void verifyEmail(String token) {

        if (token == null || token.isBlank()) {
            throw new BadRequestException("Token is required");
        }

        Long userId = jwtService.extractUserIdFromEmailVerificationToken(token);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (Boolean.TRUE.equals(user.getIsActive())) {
            throw new BadRequestException("Account already verified");
        }

        user.setIsActive(true);

        userRepository.save(user);
    }

    @Override
    public UserResponse getCurrentUser(Long userId) {
        User user = currentUserRequestCache.getUser()
                .orElseGet(() -> userRepository.findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found")));

        UserResponse response = authMapper.toUserResponse(user);

        if (RoleConstants.DOCTOR.equals(user.getRole().getName())) {
            doctorRepository.findByUserIdAndDeletedAtIsNull(userId)
                    .ifPresent(doctor -> response.setDoctorId(doctor.getId()));
        }

        return response;
    }
}
