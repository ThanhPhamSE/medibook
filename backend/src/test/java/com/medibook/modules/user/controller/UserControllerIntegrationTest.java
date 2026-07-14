package com.medibook.modules.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medibook.modules.user.dto.request.UpdateProfileRequest;
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
class UserControllerIntegrationTest {

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

    private String userToken;
    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        Role customerRole = roleRepository.findByName("CUSTOMER").orElseThrow();
        Role adminRole = roleRepository.findByName("ADMIN").orElseThrow();

        User user = new User();
        user.setEmail("user@example.com");
        user.setPassword(passwordEncoder.encode("Password123!"));
        user.setFullName("John Doe");
        user.setRole(customerRole);
        user.setIsActive(true);
        user = userRepository.save(user);

        User admin = new User();
        admin.setEmail("admin@example.com");
        admin.setPassword(passwordEncoder.encode("Admin123!"));
        admin.setFullName("Admin User");
        admin.setRole(adminRole);
        admin.setIsActive(true);
        admin = userRepository.save(admin);

        userToken = login("user@example.com", "Password123!");
        adminToken = login("admin@example.com", "Admin123!");
    }

    private String login(String email, String password) throws Exception {
        String loginRequest = String.format("{\"email\":\"%s\",\"password\":\"%s\"}", email, password);
        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("data").get("accessToken").asText();
    }

    @Test
    void getCurrentUser_Success() throws Exception {
        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User retrieved successfully"))
                .andExpect(jsonPath("$.data.email").value("user@example.com"));
    }

    @Test
    void getCurrentUser_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateProfile_Success() throws Exception {
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFullName("Jane Smith");
        request.setPhone("0987654321");

        mockMvc.perform(put("/api/v1/users/me")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Profile updated successfully"))
                .andExpect(jsonPath("$.data.fullName").value("Jane Smith"));
    }

    @Test
    void updateProfile_Unauthorized() throws Exception {
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFullName("Jane");

        mockMvc.perform(put("/api/v1/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateProfile_InvalidData() throws Exception {
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setPhone("123"); // Invalid: too short

        mockMvc.perform(put("/api/v1/users/me")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
