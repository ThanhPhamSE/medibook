package com.medibook.modules.review.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medibook.common.enums.AppointmentStatus;
import com.medibook.modules.appointment.entity.Appointment;
import com.medibook.modules.appointment.repository.AppointmentRepository;
import com.medibook.modules.doctor.entity.Doctor;
import com.medibook.modules.doctor.repository.DoctorRepository;
import com.medibook.modules.review.dto.request.ReviewCreateRequest;
import com.medibook.modules.review.entity.Review;
import com.medibook.modules.review.repository.ReviewRepository;
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
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ReviewControllerIntegrationTest {

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
    private AppointmentRepository appointmentRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private SpecialtyRepository specialtyRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String patientToken;
    private String doctorToken;
    private Long appointmentId;
    private Long doctorId;
    private LocalDateTime pastTime;

    @BeforeEach
    void setUp() throws Exception {
        Role customerRole = roleRepository.findByName("CUSTOMER").orElseThrow();
        Role doctorRole = roleRepository.findByName("DOCTOR").orElseThrow();

        User patient = new User();
        patient.setEmail("patient@example.com");
        patient.setPassword(passwordEncoder.encode("Password123!"));
        patient.setFullName("John Doe");
        patient.setRole(customerRole);
        patient.setIsActive(true);
        patient = userRepository.save(patient);

        User doctorUser = new User();
        doctorUser.setEmail("doctor@example.com");
        doctorUser.setPassword(passwordEncoder.encode("Password123!"));
        doctorUser.setFullName("Jane Smith");
        doctorUser.setRole(doctorRole);
        doctorUser.setIsActive(true);
        doctorUser = userRepository.save(doctorUser);

        Specialty specialty = new Specialty();
        specialty.setName("Cardiology");
        specialty.setDescription("Heart specialists");
        specialty = specialtyRepository.save(specialty);

        Doctor doctor = new Doctor();
        doctor.setUser(doctorUser);
        doctor.setDegree("MD");
        doctor.setExperienceYears(10);
        doctor.setConsultationFee(BigDecimal.valueOf(100));
        doctor.setBiography("Experienced doctor");
        doctor.setSpecialty(specialty);
        doctor = doctorRepository.save(doctor);
        doctorId = doctor.getId();

        pastTime = LocalDateTime.now().minusHours(2);

        Appointment appointment = new Appointment();
        appointment.setDoctor(doctor);
        appointment.setPatient(patient);
        appointment.setStartDatetime(pastTime);
        appointment.setEndDatetime(pastTime.plusMinutes(30));
        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointment.setConsultationFee(BigDecimal.valueOf(100));
        appointment = appointmentRepository.save(appointment);
        appointmentId = appointment.getId();

        patientToken = login("patient@example.com", "Password123!");
        doctorToken = login("doctor@example.com", "Password123!");
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
    void createReview_Success() throws Exception {
        ReviewCreateRequest request = new ReviewCreateRequest();
        request.setAppointmentId(appointmentId);
        request.setRating(5);
        request.setComment("Excellent service, very professional");

        mockMvc.perform(post("/api/v1/reviews")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Created successful"))
                .andExpect(jsonPath("$.data.id").exists());
    }

    @Test
    void createReview_RatingOutOfRange() throws Exception {
        ReviewCreateRequest request = new ReviewCreateRequest();
        request.setAppointmentId(appointmentId);
        request.setRating(6); // Invalid: > 5
        request.setComment("Test");

        mockMvc.perform(post("/api/v1/reviews")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Rating must be between 1 and 5")));
    }

    @Test
    void createReview_RatingBelowRange() throws Exception {
        ReviewCreateRequest request = new ReviewCreateRequest();
        request.setAppointmentId(appointmentId);
        request.setRating(0); // Invalid: < 1
        request.setComment("Test");

        mockMvc.perform(post("/api/v1/reviews")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Rating must be between 1 and 5")));
    }

    @Test
    void createReview_Unauthorized() throws Exception {
        ReviewCreateRequest request = new ReviewCreateRequest();
        request.setAppointmentId(appointmentId);
        request.setRating(5);

        mockMvc.perform(post("/api/v1/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createReview_AlreadyReviewed() throws Exception {
        ReviewCreateRequest request = new ReviewCreateRequest();
        request.setAppointmentId(appointmentId);
        request.setRating(5);
        request.setComment("First review");

        mockMvc.perform(post("/api/v1/reviews")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/reviews")
                .header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("already reviewed")));
    }

    @Test
    void getDoctorReviews_Success() throws Exception {
        mockMvc.perform(get("/api/v1/reviews/doctor/" + doctorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    void getDoctorReviews_WithPagination() throws Exception {
        mockMvc.perform(get("/api/v1/reviews/doctor/" + doctorId)
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    void getDoctorRating_Success() throws Exception {
        mockMvc.perform(get("/api/v1/reviews/doctor/" + doctorId + "/rating"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.averageRating").exists())
                .andExpect(jsonPath("$.data.totalReviews").exists());
    }

    @Test
    void getDoctorRating_NoReviews() throws Exception {
        mockMvc.perform(get("/api/v1/reviews/doctor/99999/rating"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.averageRating").value(0.0))
                .andExpect(jsonPath("$.data.totalReviews").value(0));
    }
}
