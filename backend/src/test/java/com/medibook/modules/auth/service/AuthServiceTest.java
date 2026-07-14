package com.medibook.modules.auth.service;

import com.medibook.common.exception.BadRequestException;
import com.medibook.common.exception.ResourceNotFoundException;
import com.medibook.common.exception.UnauthorizedException;
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
import com.medibook.modules.auth.service.impl.AuthServiceImpl;
import com.medibook.modules.auth.validator.AuthValidator;
import com.medibook.modules.doctor.repository.DoctorRepository;
import com.medibook.modules.notification.service.EmailService;
import com.medibook.modules.token.entity.RefreshToken;
import com.medibook.modules.token.service.RefreshTokenService;
import com.medibook.modules.user.entity.Role;
import com.medibook.modules.user.entity.User;
import com.medibook.modules.user.repository.RoleRepository;
import com.medibook.modules.user.repository.UserRepository;
import com.medibook.security.context.CurrentUserRequestCache;
import com.medibook.security.jwt.JwtProperties;
import com.medibook.security.jwt.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthValidator authValidator;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private JwtProperties jwtProperties;

    @Mock
    private AuthMapper authMapper;

    @Mock
    private EmailService emailService;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private CurrentUserRequestCache currentUserRequestCache;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User user;
    private Role role;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("Password123!");
        registerRequest.setFullName("John Doe");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("Password123!");

        role = new Role();
        role.setId(1L);
        role.setName("CUSTOMER");

        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.setFullName("John Doe");
        user.setRole(role);
        user.setIsActive(true);
    }

    @Test
    void register_Success() {
        doNothing().when(authValidator).validateRegister(any(RegisterRequest.class));
        when(roleRepository.findByName("CUSTOMER")).thenReturn(Optional.of(role));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(authMapper.toUser(any(RegisterRequest.class), anyString(), any(Role.class))).thenReturn(user);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtService.generateEmailVerificationToken(anyLong(), anyString())).thenReturn("verification-token");
        when(authMapper.toRegisterResponse(any(User.class))).thenReturn(
            RegisterResponse.builder()
                .userId(1L)
                .email("test@example.com")
                .build()
        );

        RegisterResponse response = authService.register(registerRequest);

        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        verify(userRepository).save(any(User.class));
        verify(emailService).sendVerificationEmail(anyString(), anyString());
    }

    @Test
    void register_EmailAlreadyExists() {
        doThrow(new BadRequestException("Email already exists"))
                .when(authValidator).validateRegister(any(RegisterRequest.class));

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Email already exists");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_RoleNotFound() {
        doNothing().when(authValidator).validateRegister(any(RegisterRequest.class));
        when(roleRepository.findByName("CUSTOMER")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Role not found");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_Success() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(anyLong(), anyString(), anyString())).thenReturn("access-token");
        when(refreshTokenService.createRefreshToken(any(User.class), anyString(), anyString()))
                .thenReturn("refresh-token");
        when(jwtProperties.getAccessTokenExpiration()).thenReturn(3600000L);
        when(jwtProperties.getRefreshTokenExpiration()).thenReturn(604800000L);
        when(authMapper.toLoginResponse(any(User.class), anyString(), anyString(), anyLong(), anyLong(), anyLong()))
                .thenReturn(LoginResponse.builder()
                        .accessToken("access-token")
                        .refreshToken("refresh-token")
                        .accessTokenExpiresAt(3600000L)
                        .refreshTokenExpiresAt(604800000L)
                        .issuedAt(System.currentTimeMillis())
                        .build());

        LoginResponse response = authService.login(loginRequest);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void login_InvalidCredentials() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Invalid email or password");

        verify(jwtService, never()).generateToken(anyLong(), anyString(), anyString());
    }

    @Test
    void login_UserInactive() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
        user.setIsActive(false);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Please verify your email before login");

        verify(jwtService, never()).generateToken(anyLong(), anyString(), anyString());
    }

    @Test
    void refreshToken_Success() {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash("refresh-token");

        when(refreshTokenService.verifyToken("refresh-token")).thenReturn(refreshToken);
        when(jwtService.generateToken(anyLong(), anyString(), anyString())).thenReturn("new-access-token");
        when(jwtProperties.getAccessTokenExpiration()).thenReturn(3600000L);
        when(authMapper.toLoginResponse(any(User.class), anyString(), anyString(), anyLong(), anyLong(), anyLong()))
                .thenReturn(LoginResponse.builder()
                        .accessToken("access-token")
                        .refreshToken("refresh-token")
                        .accessTokenExpiresAt(3600000L)
                        .refreshTokenExpiresAt(604800000L)
                        .issuedAt(System.currentTimeMillis())
                        .build());

        LoginResponse response = authService.refreshToken("refresh-token");

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
        verify(refreshTokenService).verifyToken("refresh-token");
        verify(jwtService).generateToken(anyLong(), anyString(), anyString());
    }

    @Test
    void refreshToken_InvalidToken() {
        when(refreshTokenService.verifyToken("invalid-token")).thenThrow(new UnauthorizedException("Invalid token"));

        assertThatThrownBy(() -> authService.refreshToken("invalid-token"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Invalid token");

        verify(jwtService, never()).generateToken(anyLong(), anyString(), anyString());
    }

    @Test
    void logout_Success() {
        doNothing().when(refreshTokenService).revokeToken("refresh-token");

        authService.logout("refresh-token");

        verify(refreshTokenService).revokeToken("refresh-token");
    }

    @Test
    void logoutAllDevices_Success() {
        doNothing().when(refreshTokenService).revokeAllUserTokens(1L);

        authService.logoutAllDevices(1L);

        verify(refreshTokenService).revokeAllUserTokens(1L);
    }

    @Test
    void changePassword_Success() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("Password123!");
        request.setNewPassword("NewPassword123!");
        request.setConfirmPassword("NewPassword123!");

        doNothing().when(authValidator).validateChangePassword(any(ChangePasswordRequest.class));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(passwordEncoder.encode(anyString())).thenReturn("new-encoded-password");
        doNothing().when(refreshTokenService).revokeAllUserTokens(1L);

        ChangePasswordResponse response = authService.changePassword(1L, request);

        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        verify(userRepository).save(any(User.class));
        verify(refreshTokenService).revokeAllUserTokens(1L);
    }

    @Test
    void changePassword_InvalidCurrentPassword() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("WrongPassword");
        request.setNewPassword("NewPassword123!");
        request.setConfirmPassword("NewPassword123!");

        doNothing().when(authValidator).validateChangePassword(any(ChangePasswordRequest.class));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThatThrownBy(() -> authService.changePassword(1L, request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Current password is incorrect");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void changePassword_UserNotFound() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("Password123!");
        request.setNewPassword("NewPassword123!");
        request.setConfirmPassword("NewPassword123!");

        doNothing().when(authValidator).validateChangePassword(any(ChangePasswordRequest.class));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.changePassword(1L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void forgotPassword_UserExists() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("test@example.com");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generatePasswordResetToken(anyLong(), anyString())).thenReturn("reset-token");

        ForgotPasswordResponse response = authService.forgotPassword(request);

        assertThat(response).isNotNull();
        assertThat(response.getMessage()).contains("password reset email been sent");
        verify(emailService).sendResetPasswordEmail("test@example.com", "reset-token");
    }

    @Test
    void forgotPassword_UserNotExists() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("nonexistent@example.com");

        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        ForgotPasswordResponse response = authService.forgotPassword(request);

        assertThat(response).isNotNull();
        assertThat(response.getMessage()).contains("If an account exists");
        verify(emailService, never()).sendResetPasswordEmail(anyString(), anyString());
    }

    @Test
    void resetPassword_Success() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setResetToken("valid-token");
        request.setNewPassword("NewPassword123!");
        request.setConfirmPassword("NewPassword123!");

        when(jwtService.extractUserIdFromResetToken("valid-token")).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(anyString())).thenReturn("new-encoded-password");
        doNothing().when(refreshTokenService).revokeAllUserTokens(1L);

        ResetPasswordResponse response = authService.resetPassword(request);

        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(1L);
        verify(userRepository).save(any(User.class));
        verify(refreshTokenService).revokeAllUserTokens(1L);
    }

    @Test
    void resetPassword_PasswordMismatch() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setResetToken("valid-token");
        request.setNewPassword("NewPassword123!");
        request.setConfirmPassword("DifferentPassword123!");

        assertThatThrownBy(() -> authService.resetPassword(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Password confirmation does not match");

        verify(jwtService, never()).extractUserIdFromResetToken(anyString());
    }

    @Test
    void resetPassword_InvalidToken() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setResetToken("invalid-token");
        request.setNewPassword("NewPassword123!");
        request.setConfirmPassword("NewPassword123!");

        when(jwtService.extractUserIdFromResetToken("invalid-token"))
                .thenThrow(new UnauthorizedException("Invalid token"));

        assertThatThrownBy(() -> authService.resetPassword(request))
                .isInstanceOf(UnauthorizedException.class);

        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    void verifyEmail_Success() {
        user.setIsActive(false);
        when(jwtService.extractUserIdFromEmailVerificationToken("valid-token")).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        authService.verifyEmail("valid-token");

        assertThat(user.getIsActive()).isTrue();
        verify(userRepository).save(user);
    }

    @Test
    void verifyEmail_InvalidToken() {
        when(jwtService.extractUserIdFromEmailVerificationToken("invalid-token"))
                .thenThrow(new UnauthorizedException("Invalid token"));

        assertThatThrownBy(() -> authService.verifyEmail("invalid-token"))
                .isInstanceOf(UnauthorizedException.class);

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void verifyEmail_AlreadyVerified() {
        user.setIsActive(true);
        when(jwtService.extractUserIdFromEmailVerificationToken("valid-token")).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.verifyEmail("valid-token"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Account already verified");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void resendVerificationEmail_Success() {
        user.setIsActive(false);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateEmailVerificationToken(anyLong(), anyString())).thenReturn("verification-token");

        authService.resendVerificationEmail("test@example.com");

        verify(emailService).sendVerificationEmail("test@example.com", "verification-token");
    }

    @Test
    void resendVerificationEmail_EmailNull() {
        assertThatThrownBy(() -> authService.resendVerificationEmail(null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Email is required");

        verify(emailService, never()).sendVerificationEmail(anyString(), anyString());
    }

    @Test
    void resendVerificationEmail_UserNotFound() {
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.resendVerificationEmail("nonexistent@example.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(emailService, never()).sendVerificationEmail(anyString(), anyString());
    }

    @Test
    void resendVerificationEmail_AlreadyVerified() {
        user.setIsActive(true);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.resendVerificationEmail("test@example.com"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Email already verified");

        verify(emailService, never()).sendVerificationEmail(anyString(), anyString());
    }
}
