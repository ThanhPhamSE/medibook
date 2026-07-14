package com.medibook.modules.specialty.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medibook.modules.specialty.dto.request.SpecialtyCreateRequest;
import com.medibook.modules.specialty.dto.request.SpecialtyUpdateRequest;
import com.medibook.modules.specialty.entity.Specialty;
import com.medibook.modules.specialty.repository.SpecialtyRepository;
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
class SpecialtyControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private SpecialtyRepository specialtyRepository;

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
    void getAllSpecialties_Success() throws Exception {
        mockMvc.perform(get("/api/v1/specialties"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    void getAllSpecialties_WithKeyword() throws Exception {
        mockMvc.perform(get("/api/v1/specialties")
                        .param("keyword", "Cardio"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    void getAllSpecialties_WithPagination() throws Exception {
        mockMvc.perform(get("/api/v1/specialties")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    void getSpecialtyById_Success() throws Exception {
        Specialty specialty = new Specialty();
        specialty.setName("Cardiology");
        specialty.setDescription("Heart specialists");
        specialty = specialtyRepository.save(specialty);

        mockMvc.perform(get("/api/v1/specialties/" + specialty.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(specialty.getId()))
                .andExpect(jsonPath("$.data.name").value("Cardiology"));
    }

    @Test
    void getSpecialtyById_NotFound() throws Exception {
        mockMvc.perform(get("/api/v1/specialties/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("Specialty not found")));
    }

    @Test
    void createSpecialty_Success() throws Exception {
        SpecialtyCreateRequest request = new SpecialtyCreateRequest();
        request.setName("Neurology");
        request.setDescription("Brain and nervous system specialists");

        mockMvc.perform(post("/api/v1/specialties")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Created successful"))
                .andExpect(jsonPath("$.data.name").value("Neurology"));
    }

    @Test
    void createSpecialty_Forbidden() throws Exception {
        SpecialtyCreateRequest request = new SpecialtyCreateRequest();
        request.setName("Neurology");

        mockMvc.perform(post("/api/v1/specialties")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createSpecialty_Unauthorized() throws Exception {
        SpecialtyCreateRequest request = new SpecialtyCreateRequest();
        request.setName("Neurology");

        mockMvc.perform(post("/api/v1/specialties")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createSpecialty_AlreadyExists() throws Exception {
        Specialty specialty = new Specialty();
        specialty.setName("Cardiology");
        specialty.setDescription("Heart specialists");
        specialtyRepository.save(specialty);

        SpecialtyCreateRequest request = new SpecialtyCreateRequest();
        request.setName("Cardiology");
        request.setDescription("Another description");

        mockMvc.perform(post("/api/v1/specialties")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("already exists")));
    }

    @Test
    void updateSpecialty_Success() throws Exception {
        Specialty specialty = new Specialty();
        specialty.setName("Cardiology");
        specialty.setDescription("Heart specialists");
        specialty = specialtyRepository.save(specialty);

        SpecialtyUpdateRequest request = new SpecialtyUpdateRequest();
        request.setName("Updated Cardiology");
        request.setDescription("Updated description");

        mockMvc.perform(put("/api/v1/specialties/" + specialty.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Updated successful"))
                .andExpect(jsonPath("$.data.name").value("Updated Cardiology"));
    }

    @Test
    void updateSpecialty_Forbidden() throws Exception {
        Specialty specialty = new Specialty();
        specialty.setName("Cardiology");
        specialty = specialtyRepository.save(specialty);

        SpecialtyUpdateRequest request = new SpecialtyUpdateRequest();
        request.setName("Updated");

        mockMvc.perform(put("/api/v1/specialties/" + specialty.getId())
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateSpecialty_NameConflict() throws Exception {
        Specialty specialty1 = new Specialty();
        specialty1.setName("Cardiology");
        specialty1 = specialtyRepository.save(specialty1);

        Specialty specialty2 = new Specialty();
        specialty2.setName("Neurology");
        specialty2 = specialtyRepository.save(specialty2);

        SpecialtyUpdateRequest request = new SpecialtyUpdateRequest();
        request.setName("Neurology"); // Conflict with specialty2

        mockMvc.perform(put("/api/v1/specialties/" + specialty1.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("already exists")));
    }

    @Test
    void deleteSpecialty_Success() throws Exception {
        Specialty specialty = new Specialty();
        specialty.setName("Cardiology");
        specialty = specialtyRepository.save(specialty);

        mockMvc.perform(delete("/api/v1/specialties/" + specialty.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Deleted successful"));
    }

    @Test
    void deleteSpecialty_Forbidden() throws Exception {
        Specialty specialty = new Specialty();
        specialty.setName("Cardiology");
        specialty = specialtyRepository.save(specialty);

        mockMvc.perform(delete("/api/v1/specialties/" + specialty.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteSpecialty_Unauthorized() throws Exception {
        Specialty specialty = new Specialty();
        specialty.setName("Cardiology");
        specialty = specialtyRepository.save(specialty);

        mockMvc.perform(delete("/api/v1/specialties/" + specialty.getId()))
                .andExpect(status().isUnauthorized());
    }
}
