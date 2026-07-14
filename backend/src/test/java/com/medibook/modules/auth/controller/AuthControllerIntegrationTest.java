package com.medibook.modules.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medibook.modules.auth.dto.request.ChangePasswordRequest;
import com.medibook.modules.auth.dto.request.ForgotPasswordRequest;
import com.medibook.modules.auth.dto.request.LoginRequest;
import com.medibook.modules.auth.dto.request.LogoutRequest;
import com.medibook.modules.auth.dto.request.RefreshTokenRequest;
import com.medibook.modules.auth.dto.request.RegisterRequest;
import com.medibook.modules.auth.dto.request.ResetPasswordRequest;
import com.medibook.modules.user.entity.Role;
import com.medibook.modules.user.entity.User;
import com.medibook.modules.user.repository.RoleRepository;
import com.medibook.modules.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private RoleRepository roleRepository;

        @Autowired
        private PasswordEncoder passwordEncoder;

        private RegisterRequest registerRequest;
        private LoginRequest loginRequest;

        @BeforeEach
        void setUp() {
                registerRequest = new RegisterRequest();
                registerRequest.setEmail("test@example.com");
                registerRequest.setPassword("Password123!");
                registerRequest.setFullName("John Doe");
                registerRequest.setPhone("1234567890");

                loginRequest = new LoginRequest();
                loginRequest.setEmail("test@example.com");
                loginRequest.setPassword("Password123!");
        }

        @Test
        void register_Success() throws Exception {
                mockMvc.perform(post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.status").value(201))
                                .andExpect(jsonPath("$.message").value("Register successful"))
                                .andExpect(jsonPath("$.data.userId").exists())
                                .andExpect(jsonPath("$.data.email").value("test@example.com"));
        }

        @Test
        void register_EmailAlreadyExists() throws Exception {
                registerRequest.setEmail("existing@example.com");

                mockMvc.perform(post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest)))
                                .andExpect(status().isCreated());

                mockMvc.perform(post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest)))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.message").value(containsString("already exists")));
        }

        @Test
        void register_InvalidEmail() throws Exception {
                registerRequest.setEmail("invalid-email");

                mockMvc.perform(post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void register_WeakPassword() throws Exception {
                registerRequest.setPassword("weak");

                mockMvc.perform(post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void login_Success() throws Exception {
                mockMvc.perform(post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest)))
                                .andExpect(status().isCreated());

                mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("Login successful"))
                                .andExpect(jsonPath("$.data.accessToken").exists())
                                .andExpect(jsonPath("$.data.refreshToken").exists())
                                .andExpect(jsonPath("$.data.expiresIn").exists());
        }

        @Test
        void login_InvalidCredentials() throws Exception {
                loginRequest.setPassword("WrongPassword123!");

                mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.message").value(containsString("Invalid email or password")));
        }

        @Test
        void login_UserNotVerified() throws Exception {
                mockMvc.perform(post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest)))
                                .andExpect(status().isCreated());

                User user = userRepository.findByEmail("test@example.com").orElseThrow();
                user.setIsActive(false);
                userRepository.save(user);

                mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.message").value(containsString("verify your email")));
        }

        @Test
        void refreshToken_Success() throws Exception {
                mockMvc.perform(post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest)))
                                .andExpect(status().isCreated());

                String loginResponse = mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andReturn().getResponse().getContentAsString();

                String refreshToken = objectMapper.readTree(loginResponse)
                                .get("data").get("refreshToken").asText();

                RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest();
                refreshTokenRequest.setRefreshToken(refreshToken);

                mockMvc.perform(post("/api/v1/auth/refresh-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(refreshTokenRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("Refresh token successful"))
                                .andExpect(jsonPath("$.data.accessToken").exists());
        }

        @Test
        void refreshToken_InvalidToken() throws Exception {
                RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest();
                refreshTokenRequest.setRefreshToken("invalid-token");

                mockMvc.perform(post("/api/v1/auth/refresh-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(refreshTokenRequest)))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void logout_Success() throws Exception {
                mockMvc.perform(post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest)))
                                .andExpect(status().isCreated());

                String loginResponse = mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andReturn().getResponse().getContentAsString();

                String refreshToken = objectMapper.readTree(loginResponse)
                                .get("data").get("refreshToken").asText();

                LogoutRequest logoutRequest = new LogoutRequest();
                logoutRequest.setRefreshToken(refreshToken);

                mockMvc.perform(post("/api/v1/auth/logout")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(logoutRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("Logout successful"));
        }

        @Test
        void logoutAll_Success() throws Exception {
                mockMvc.perform(post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest)))
                                .andExpect(status().isCreated());

                String loginResponse = mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andReturn().getResponse().getContentAsString();

                String accessToken = objectMapper.readTree(loginResponse)
                                .get("data").get("accessToken").asText();

                mockMvc.perform(post("/api/v1/auth/logout-all")
                                .header("Authorization", "Bearer " + accessToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("Logout all devices successful"));
        }

        @Test
        void logoutAll_Unauthorized() throws Exception {
                mockMvc.perform(post("/api/v1/auth/logout-all"))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void changePassword_Success() throws Exception {
                mockMvc.perform(post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest)))
                                .andExpect(status().isCreated());

                String loginResponse = mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andReturn().getResponse().getContentAsString();

                String accessToken = objectMapper.readTree(loginResponse)
                                .get("data").get("accessToken").asText();

                ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest();
                changePasswordRequest.setCurrentPassword("Password123!");
                changePasswordRequest.setNewPassword("NewPassword123!");
                changePasswordRequest.setConfirmPassword("NewPassword123!");

                mockMvc.perform(post("/api/v1/auth/change-password")
                                .header("Authorization", "Bearer " + accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(changePasswordRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message")
                                                .value(containsString("Password changed successfully")));
        }

        @Test
        void changePassword_InvalidCurrentPassword() throws Exception {
                mockMvc.perform(post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest)))
                                .andExpect(status().isCreated());

                String loginResponse = mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andReturn().getResponse().getContentAsString();

                String accessToken = objectMapper.readTree(loginResponse)
                                .get("data").get("accessToken").asText();

                ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest();
                changePasswordRequest.setCurrentPassword("WrongPassword");
                changePasswordRequest.setNewPassword("NewPassword123!");
                changePasswordRequest.setConfirmPassword("NewPassword123!");

                mockMvc.perform(post("/api/v1/auth/change-password")
                                .header("Authorization", "Bearer " + accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(changePasswordRequest)))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.message")
                                                .value(containsString("Current password is incorrect")));
        }

        @Test
        void forgotPassword_Success() throws Exception {
                ForgotPasswordRequest forgotPasswordRequest = new ForgotPasswordRequest();
                forgotPasswordRequest.setEmail("test@example.com");

                mockMvc.perform(post("/api/v1/auth/forgot-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(forgotPasswordRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message")
                                                .value(containsString("password reset email been sent")));
        }

        @Test
        void forgotPassword_UserNotExists() throws Exception {
                ForgotPasswordRequest forgotPasswordRequest = new ForgotPasswordRequest();
                forgotPasswordRequest.setEmail("nonexistent@example.com");

                mockMvc.perform(post("/api/v1/auth/forgot-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(forgotPasswordRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value(containsString("If an account exists")));
        }

        @Test
        void resetPassword_Success() throws Exception {
                mockMvc.perform(post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest)))
                                .andExpect(status().isCreated());

                ResetPasswordRequest resetPasswordRequest = new ResetPasswordRequest();
                resetPasswordRequest.setResetToken("valid-token");
                resetPasswordRequest.setNewPassword("NewPassword123!");
                resetPasswordRequest.setConfirmPassword("NewPassword123!");

                mockMvc.perform(post("/api/v1/auth/reset-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(resetPasswordRequest)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void resetPassword_PasswordMismatch() throws Exception {
                ResetPasswordRequest resetPasswordRequest = new ResetPasswordRequest();
                resetPasswordRequest.setResetToken("valid-token");
                resetPasswordRequest.setNewPassword("NewPassword123!");
                resetPasswordRequest.setConfirmPassword("DifferentPassword123!");

                mockMvc.perform(post("/api/v1/auth/reset-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(resetPasswordRequest)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.message")
                                                .value(containsString("Password confirmation does not match")));
        }

        @Test
        void verifyEmail_Success() throws Exception {
                mockMvc.perform(post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest)))
                                .andExpect(status().isCreated());

                mockMvc.perform(get("/api/v1/auth/verify-email")
                                .param("token", "valid-token"))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void verifyEmail_InvalidToken() throws Exception {
                mockMvc.perform(get("/api/v1/auth/verify-email")
                                .param("token", "invalid-token"))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void getCurrentUser_Success() throws Exception {
                mockMvc.perform(post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest)))
                                .andExpect(status().isCreated());

                String loginResponse = mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andReturn().getResponse().getContentAsString();

                String accessToken = objectMapper.readTree(loginResponse)
                                .get("data").get("accessToken").asText();

                mockMvc.perform(get("/api/v1/auth/me")
                                .header("Authorization", "Bearer " + accessToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("User retrieved successfully"))
                                .andExpect(jsonPath("$.data.email").value("test@example.com"));
        }

        @Test
        void getCurrentUser_Unauthorized() throws Exception {
                mockMvc.perform(get("/api/v1/auth/me"))
                                .andExpect(status().isUnauthorized());
        }
}
