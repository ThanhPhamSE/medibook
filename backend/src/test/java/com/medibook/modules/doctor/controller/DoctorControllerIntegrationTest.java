package com.medibook.modules.doctor.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medibook.modules.doctor.dto.request.CreateDoctorRequest;
import com.medibook.modules.doctor.dto.request.DoctorSearchRequest;
import com.medibook.modules.doctor.dto.request.UpdateDoctorRequest;
import com.medibook.modules.doctor.entity.Doctor;
import com.medibook.modules.doctor.repository.DoctorRepository;
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

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class DoctorControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private SpecialtyRepository specialtyRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String userToken;
    private String doctorToken;
    private String adminToken;
    private Long specialtyId;
    private Long doctorUserId;

    @BeforeEach
    void setUp() throws Exception {
        Role customerRole = roleRepository.findByName("CUSTOMER").orElseThrow();
        Role doctorRole = roleRepository.findByName("DOCTOR").orElseThrow();
        Role adminRole = roleRepository.findByName("ADMIN").orElseThrow();

        User user = new User();
        user.setEmail("user@example.com");
        user.setPassword(passwordEncoder.encode("Password123!"));
        user.setFullName("John Doe");
        user.setRole(customerRole);
        user.setIsActive(true);
        user = userRepository.save(user);

        User doctorUser = new User();
        doctorUser.setEmail("doctor@example.com");
        doctorUser.setPassword(passwordEncoder.encode("Password123!"));
        doctorUser.setFullName("Jane Smith");
        doctorUser.setRole(doctorRole);
        doctorUser.setIsActive(true);
        doctorUser = userRepository.save(doctorUser);
        doctorUserId = doctorUser.getId();

        User admin = new User();
        admin.setEmail("admin@example.com");
        admin.setPassword(passwordEncoder.encode("Admin123!"));
        admin.setFullName("Admin User");
        admin.setRole(adminRole);
        admin.setIsActive(true);
        admin = userRepository.save(admin);

        Specialty specialty = new Specialty();
        specialty.setName("Cardiology");
        specialty.setDescription("Heart specialists");
        specialty = specialtyRepository.save(specialty);
        specialtyId = specialty.getId();

        Doctor doctor = new Doctor();
        doctor.setUser(doctorUser);
        doctor.setDegree("MD");
        doctor.setExperienceYears(10);
        doctor.setConsultationFee(BigDecimal.valueOf(100));
        doctor.setBiography("Experienced doctor");
        doctor.setSpecialty(specialty);
        doctor = doctorRepository.save(doctor);

        userToken = login("user@example.com", "Password123!");
        doctorToken = login("doctor@example.com", "Password123!");
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
    void searchDoctors_Success() throws Exception {
        mockMvc.perform(get("/api/v1/doctors")
                .param("keyword", "Cardiology"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    void searchDoctors_WithFilters() throws Exception {
        mockMvc.perform(get("/api/v1/doctors")
                .param("specialtyId", specialtyId.toString())
                .param("minExperience", "5")
                .param("minFee", "50")
                .param("maxFee", "200"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    void getDoctorById_Success() throws Exception {
        mockMvc.perform(get("/api/v1/doctors/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    void getDoctorById_NotFound() throws Exception {
        mockMvc.perform(get("/api/v1/doctors/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("Doctor not found")));
    }

    @Test
    void createDoctor_Success() throws Exception {
        CreateDoctorRequest request = new CreateDoctorRequest();
        request.setUserId(doctorUserId);
        request.setSpecialtyId(specialtyId);
        request.setDegree("MD");
        request.setExperienceYears(10);
        request.setConsultationFee(BigDecimal.valueOf(100));
        request.setBiography("Experienced cardiologist");

        mockMvc.perform(post("/api/v1/doctors")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Doctor profile created successfully"))
                .andExpect(jsonPath("$.data.id").exists());
    }

    @Test
    void createDoctor_Forbidden() throws Exception {
        CreateDoctorRequest request = new CreateDoctorRequest();
        request.setUserId(doctorUserId);
        request.setSpecialtyId(specialtyId);
        request.setDegree("MD");

        mockMvc.perform(post("/api/v1/doctors")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createDoctor_Unauthorized() throws Exception {
        CreateDoctorRequest request = new CreateDoctorRequest();
        request.setUserId(doctorUserId);
        request.setSpecialtyId(specialtyId);

        mockMvc.perform(post("/api/v1/doctors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateDoctor_Success() throws Exception {
        UpdateDoctorRequest request = new UpdateDoctorRequest();
        request.setSpecialtyId(specialtyId);
        request.setDegree("PhD");
        request.setExperienceYears(15);
        request.setConsultationFee(BigDecimal.valueOf(150));
        request.setBiography("Updated biography");

        mockMvc.perform(put("/api/v1/doctors/1")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    void updateDoctor_Forbidden() throws Exception {
        UpdateDoctorRequest request = new UpdateDoctorRequest();
        request.setDegree("PhD");

        mockMvc.perform(put("/api/v1/doctors/1")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteDoctor_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/doctors/1")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Deleted successful"));
    }

    @Test
    void deleteDoctor_Forbidden() throws Exception {
        mockMvc.perform(delete("/api/v1/doctors/1")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void upgradeToDoctor_Success() throws Exception {
        User newUser = new User();
        newUser.setEmail("newuser@example.com");
        newUser.setPassword(passwordEncoder.encode("Password123!"));
        newUser.setFullName("New User");
        newUser.setRole(roleRepository.findByName("CUSTOMER").orElseThrow());
        newUser.setIsActive(true);
        newUser = userRepository.save(newUser);

        var request = new com.medibook.modules.doctor.dto.request.UpgradeToDoctorRequest();
        request.setUserId(newUser.getId());
        request.setSpecialtyId(specialtyId);
        request.setDegree("MD");
        request.setExperienceYears(5);
        request.setConsultationFee(BigDecimal.valueOf(80));

        mockMvc.perform(post("/api/v1/doctors/upgrade-to-doctor")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Updated successful"));
    }
}
