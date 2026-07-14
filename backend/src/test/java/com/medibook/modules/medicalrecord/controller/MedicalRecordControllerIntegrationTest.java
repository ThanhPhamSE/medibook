package com.medibook.modules.medicalrecord.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medibook.common.enums.AppointmentStatus;
import com.medibook.modules.appointment.entity.Appointment;
import com.medibook.modules.appointment.repository.AppointmentRepository;
import com.medibook.modules.doctor.entity.Doctor;
import com.medibook.modules.doctor.repository.DoctorRepository;
import com.medibook.modules.medicalrecord.dto.request.MedicalRecordCreateRequest;
import com.medibook.modules.medicalrecord.dto.request.MedicalRecordUpdateRequest;
import com.medibook.modules.medicalrecord.entity.MedicalRecord;
import com.medibook.modules.medicalrecord.repository.MedicalRecordRepository;
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
class MedicalRecordControllerIntegrationTest {

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
        private MedicalRecordRepository medicalRecordRepository;

        @Autowired
        private SpecialtyRepository specialtyRepository;

        @Autowired
        private PasswordEncoder passwordEncoder;

        private String patientToken;
        private String doctorToken;
        private String adminToken;
        private Long appointmentId;
        private LocalDateTime pastTime;

        @BeforeEach
        void setUp() throws Exception {
                Role customerRole = roleRepository.findByName("CUSTOMER").orElseThrow();
                Role doctorRole = roleRepository.findByName("DOCTOR").orElseThrow();
                Role adminRole = roleRepository.findByName("ADMIN").orElseThrow();

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

                Doctor doctor = new Doctor();
                doctor.setUser(doctorUser);
                doctor.setDegree("MD");
                doctor.setExperienceYears(10);
                doctor.setConsultationFee(BigDecimal.valueOf(100));
                doctor.setBiography("Experienced doctor");
                doctor.setSpecialty(specialty);
                doctor = doctorRepository.save(doctor);

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
        void createMedicalRecord_Success() throws Exception {
                MedicalRecordCreateRequest request = new MedicalRecordCreateRequest();
                request.setAppointmentId(appointmentId);
                request.setDiagnosis("Hypertension");
                request.setPrescription("Medication and lifestyle changes");
                request.setNote("Patient should monitor blood pressure daily");

                mockMvc.perform(post("/api/v1/medical-records")
                                .header("Authorization", "Bearer " + doctorToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.message").value("Created successful"))
                                .andExpect(jsonPath("$.data.id").exists());
        }

        @Test
        void createMedicalRecord_Forbidden() throws Exception {
                MedicalRecordCreateRequest request = new MedicalRecordCreateRequest();
                request.setAppointmentId(appointmentId);
                request.setDiagnosis("Test");

                mockMvc.perform(post("/api/v1/medical-records")
                                .header("Authorization", "Bearer " + patientToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isForbidden());
        }

        @Test
        void createMedicalRecord_Unauthorized() throws Exception {
                MedicalRecordCreateRequest request = new MedicalRecordCreateRequest();
                request.setAppointmentId(appointmentId);

                mockMvc.perform(post("/api/v1/medical-records")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void getMedicalRecord_Success() throws Exception {
                MedicalRecordCreateRequest createRequest = new MedicalRecordCreateRequest();
                createRequest.setAppointmentId(appointmentId);
                createRequest.setDiagnosis("Test diagnosis");

                mockMvc.perform(post("/api/v1/medical-records")
                                .header("Authorization", "Bearer " + doctorToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest)))
                                .andExpect(status().isCreated());

                mockMvc.perform(get("/api/v1/medical-records/1")
                                .header("Authorization", "Bearer " + doctorToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.id").value(1));
        }

        @Test
        void getMedicalRecord_NotFound() throws Exception {
                mockMvc.perform(get("/api/v1/medical-records/99999")
                                .header("Authorization", "Bearer " + doctorToken))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.message").value(containsString("Medical record not found")));
        }

        @Test
        void updateMedicalRecord_Success() throws Exception {
                MedicalRecordCreateRequest createRequest = new MedicalRecordCreateRequest();
                createRequest.setAppointmentId(appointmentId);
                createRequest.setDiagnosis("Test diagnosis");

                mockMvc.perform(post("/api/v1/medical-records")
                                .header("Authorization", "Bearer " + doctorToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest)))
                                .andExpect(status().isCreated());

                MedicalRecordUpdateRequest updateRequest = new MedicalRecordUpdateRequest();
                updateRequest.setDiagnosis("Updated diagnosis");
                updateRequest.setPrescription("Updated treatment");

                mockMvc.perform(put("/api/v1/medical-records/1")
                                .header("Authorization", "Bearer " + doctorToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("Updated successful"));
        }

        @Test
        void deleteMedicalRecord_Success() throws Exception {
                MedicalRecordCreateRequest createRequest = new MedicalRecordCreateRequest();
                createRequest.setAppointmentId(appointmentId);
                createRequest.setDiagnosis("Test diagnosis");

                mockMvc.perform(post("/api/v1/medical-records")
                                .header("Authorization", "Bearer " + doctorToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest)))
                                .andExpect(status().isCreated());

                mockMvc.perform(delete("/api/v1/medical-records/1")
                                .header("Authorization", "Bearer " + doctorToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("Deleted successful"));
        }

        @Test
        void getMyMedicalRecords_Success() throws Exception {
                mockMvc.perform(get("/api/v1/medical-records/me")
                                .header("Authorization", "Bearer " + patientToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data").exists());
        }

        @Test
        void getDoctorMedicalRecords_Success() throws Exception {
                mockMvc.perform(get("/api/v1/medical-records/doctor")
                                .header("Authorization", "Bearer " + doctorToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data").exists());
        }

        @Test
        void getAllMedicalRecords_Admin_Success() throws Exception {
                mockMvc.perform(get("/api/v1/medical-records")
                                .header("Authorization", "Bearer " + adminToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data").exists());
        }

        @Test
        void getAllMedicalRecords_Forbidden() throws Exception {
                mockMvc.perform(get("/api/v1/medical-records")
                                .header("Authorization", "Bearer " + patientToken))
                                .andExpect(status().isForbidden());
        }
}
