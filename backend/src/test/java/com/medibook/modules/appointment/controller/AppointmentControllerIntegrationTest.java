package com.medibook.modules.appointment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medibook.common.enums.AppointmentStatus;
import com.medibook.modules.appointment.dto.request.AppointmentCreateRequest;
import com.medibook.modules.appointment.dto.request.AppointmentRescheduleRequest;
import com.medibook.modules.doctor.entity.Doctor;
import com.medibook.modules.doctor.repository.DoctorRepository;
import com.medibook.modules.schedule.entity.DoctorWorkingPattern;
import com.medibook.modules.schedule.repository.DoctorWorkingPatternRepository;
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
import java.time.LocalTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AppointmentControllerIntegrationTest {

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
        private DoctorWorkingPatternRepository workingPatternRepository;

        @Autowired
        private PasswordEncoder passwordEncoder;

        private String patientToken;
        private String doctorToken;
        private String adminToken;
        private Long doctorId;
        private LocalDateTime futureTime;

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

                Doctor doctor = new Doctor();
                doctor.setUser(doctorUser);
                doctor.setDegree("MD");
                doctor.setExperienceYears(10);
                doctor.setConsultationFee(BigDecimal.valueOf(100));
                doctor.setBiography("Experienced doctor");
                doctor = doctorRepository.save(doctor);
                doctorId = doctor.getId();

                DoctorWorkingPattern pattern = new DoctorWorkingPattern();
                pattern.setDoctor(doctor);
                pattern.setDayOfWeek(com.medibook.common.enums.DayOfWeekEnum.MON);
                pattern.setStartTime(LocalTime.of(9, 0));
                pattern.setEndTime(LocalTime.of(17, 0));
                pattern.setSlotDuration(30);
                pattern.setBufferDuration(5);
                workingPatternRepository.save(pattern);

                futureTime = LocalDateTime.now().plusDays(2).withHour(10).withMinute(0).withSecond(0).withNano(0);

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
        void createAppointment_Success() throws Exception {
                AppointmentCreateRequest request = new AppointmentCreateRequest();
                request.setDoctorId(doctorId);
                request.setStartDateTime(futureTime);
                request.setNote("Test appointment");

                mockMvc.perform(post("/api/v1/appointments")
                                .header("Authorization", "Bearer " + patientToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.status").value(201))
                                .andExpect(jsonPath("$.message").value("Appointment created successfully"))
                                .andExpect(jsonPath("$.data.id").exists())
                                .andExpect(jsonPath("$.data.status").value("PENDING"));
        }

        @Test
        void createAppointment_Unauthorized() throws Exception {
                AppointmentCreateRequest request = new AppointmentCreateRequest();
                request.setDoctorId(doctorId);
                request.setStartDateTime(futureTime);

                mockMvc.perform(post("/api/v1/appointments")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void createAppointment_DoctorNotFound() throws Exception {
                AppointmentCreateRequest request = new AppointmentCreateRequest();
                request.setDoctorId(99999L);
                request.setStartDateTime(futureTime);

                mockMvc.perform(post("/api/v1/appointments")
                                .header("Authorization", "Bearer " + patientToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.message").value(containsString("Doctor not found")));
        }

        @Test
        void createAppointment_InvalidTime() throws Exception {
                AppointmentCreateRequest request = new AppointmentCreateRequest();
                request.setDoctorId(doctorId);
                request.setStartDateTime(LocalDateTime.now().minusHours(1));

                mockMvc.perform(post("/api/v1/appointments")
                                .header("Authorization", "Bearer " + patientToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void getAppointment_Success() throws Exception {
                AppointmentCreateRequest request = new AppointmentCreateRequest();
                request.setDoctorId(doctorId);
                request.setStartDateTime(futureTime);

                String response = mockMvc.perform(post("/api/v1/appointments")
                                .header("Authorization", "Bearer " + patientToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andReturn().getResponse().getContentAsString();

                Long appointmentId = objectMapper.readTree(response).get("data").get("id").asLong();

                mockMvc.perform(get("/api/v1/appointments/" + appointmentId)
                                .header("Authorization", "Bearer " + patientToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.id").value(appointmentId));
        }

        @Test
        void getAppointment_NotFound() throws Exception {
                mockMvc.perform(get("/api/v1/appointments/99999")
                                .header("Authorization", "Bearer " + patientToken))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.message").value(containsString("Appointment not found")));
        }

        @Test
        void getAppointment_Unauthorized() throws Exception {
                mockMvc.perform(get("/api/v1/appointments/1"))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void getMyAppointments_Success() throws Exception {
                mockMvc.perform(get("/api/v1/appointments/me")
                                .header("Authorization", "Bearer " + patientToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data").exists());
        }

        @Test
        void getMyAppointments_WithStatusFilter() throws Exception {
                mockMvc.perform(get("/api/v1/appointments/me")
                                .header("Authorization", "Bearer " + patientToken)
                                .param("status", "PENDING"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data").exists());
        }

        @Test
        void getMyAppointments_Unauthorized() throws Exception {
                mockMvc.perform(get("/api/v1/appointments/me"))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void cancelAppointment_Success() throws Exception {
                AppointmentCreateRequest request = new AppointmentCreateRequest();
                request.setDoctorId(doctorId);
                request.setStartDateTime(futureTime);

                String response = mockMvc.perform(post("/api/v1/appointments")
                                .header("Authorization", "Bearer " + patientToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andReturn().getResponse().getContentAsString();

                Long appointmentId = objectMapper.readTree(response).get("data").get("id").asLong();

                mockMvc.perform(put("/api/v1/appointments/" + appointmentId + "/cancel")
                                .header("Authorization", "Bearer " + patientToken)
                                .param("reason", "Test cancellation"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("success"));
        }

        @Test
        void cancelAppointment_Unauthorized() throws Exception {
                mockMvc.perform(put("/api/v1/appointments/1/cancel")
                                .param("reason", "Test"))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void cancelAppointment_LessThan24Hours() throws Exception {
                AppointmentCreateRequest request = new AppointmentCreateRequest();
                request.setDoctorId(doctorId);
                request.setStartDateTime(LocalDateTime.now().plusHours(12));

                String response = mockMvc.perform(post("/api/v1/appointments")
                                .header("Authorization", "Bearer " + patientToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andReturn().getResponse().getContentAsString();

                Long appointmentId = objectMapper.readTree(response).get("data").get("id").asLong();

                mockMvc.perform(put("/api/v1/appointments/" + appointmentId + "/cancel")
                                .header("Authorization", "Bearer " + patientToken)
                                .param("reason", "Test"))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.message").value(containsString("24 hours")));
        }

        @Test
        void confirmAppointment_Success() throws Exception {
                AppointmentCreateRequest request = new AppointmentCreateRequest();
                request.setDoctorId(doctorId);
                request.setStartDateTime(futureTime);

                String response = mockMvc.perform(post("/api/v1/appointments")
                                .header("Authorization", "Bearer " + patientToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andReturn().getResponse().getContentAsString();

                Long appointmentId = objectMapper.readTree(response).get("data").get("id").asLong();

                mockMvc.perform(patch("/api/v1/appointments/" + appointmentId + "/confirm")
                                .header("Authorization", "Bearer " + doctorToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("success"));
        }

        @Test
        void confirmAppointment_Forbidden() throws Exception {
                mockMvc.perform(patch("/api/v1/appointments/1/confirm")
                                .header("Authorization", "Bearer " + patientToken))
                                .andExpect(status().isForbidden());
        }

        @Test
        void completeAppointment_Success() throws Exception {
                AppointmentCreateRequest request = new AppointmentCreateRequest();
                request.setDoctorId(doctorId);
                request.setStartDateTime(LocalDateTime.now().minusHours(1));

                String response = mockMvc.perform(post("/api/v1/appointments")
                                .header("Authorization", "Bearer " + patientToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andReturn().getResponse().getContentAsString();

                Long appointmentId = objectMapper.readTree(response).get("data").get("id").asLong();

                mockMvc.perform(patch("/api/v1/appointments/" + appointmentId + "/complete")
                                .header("Authorization", "Bearer " + doctorToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("success"));
        }

        @Test
        void markNoShow_Success() throws Exception {
                AppointmentCreateRequest request = new AppointmentCreateRequest();
                request.setDoctorId(doctorId);
                request.setStartDateTime(LocalDateTime.now().minusHours(1));

                String response = mockMvc.perform(post("/api/v1/appointments")
                                .header("Authorization", "Bearer " + patientToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andReturn().getResponse().getContentAsString();

                Long appointmentId = objectMapper.readTree(response).get("data").get("id").asLong();

                mockMvc.perform(patch("/api/v1/appointments/" + appointmentId + "/no-show")
                                .header("Authorization", "Bearer " + doctorToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("success"));
        }

        @Test
        void rescheduleAppointment_Success() throws Exception {
                AppointmentCreateRequest request = new AppointmentCreateRequest();
                request.setDoctorId(doctorId);
                request.setStartDateTime(futureTime);

                String response = mockMvc.perform(post("/api/v1/appointments")
                                .header("Authorization", "Bearer " + patientToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andReturn().getResponse().getContentAsString();

                Long appointmentId = objectMapper.readTree(response).get("data").get("id").asLong();

                AppointmentRescheduleRequest rescheduleRequest = new AppointmentRescheduleRequest();
                rescheduleRequest.setNewStartDatetime(futureTime.plusDays(1));

                mockMvc.perform(put("/api/v1/appointments/" + appointmentId + "/reschedule")
                                .header("Authorization", "Bearer " + patientToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(rescheduleRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.id").value(appointmentId));
        }

        @Test
        void rescheduleAppointment_Forbidden() throws Exception {
                AppointmentRescheduleRequest rescheduleRequest = new AppointmentRescheduleRequest();
                rescheduleRequest.setNewStartDatetime(futureTime.plusDays(1));

                mockMvc.perform(put("/api/v1/appointments/1/reschedule")
                                .header("Authorization", "Bearer " + doctorToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(rescheduleRequest)))
                                .andExpect(status().isForbidden());
        }

        @Test
        void isSlotBooked_Success() throws Exception {
                mockMvc.perform(get("/api/v1/appointments/slots/check")
                                .header("Authorization", "Bearer " + patientToken)
                                .param("doctorId", doctorId.toString())
                                .param("startDatetime", futureTime.toString()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data").isBoolean());
        }

        @Test
        void getTodayAppointments_Success() throws Exception {
                mockMvc.perform(get("/api/v1/appointments/doctor/today")
                                .header("Authorization", "Bearer " + doctorToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data").exists());
        }

        @Test
        void getTodayAppointments_Forbidden() throws Exception {
                mockMvc.perform(get("/api/v1/appointments/doctor/today")
                                .header("Authorization", "Bearer " + patientToken))
                                .andExpect(status().isForbidden());
        }

        @Test
        void getAllBookings_Admin_Success() throws Exception {
                mockMvc.perform(get("/api/v1/appointments/admin/bookings")
                                .header("Authorization", "Bearer " + adminToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data").exists());
        }

        @Test
        void getAllBookings_Forbidden() throws Exception {
                mockMvc.perform(get("/api/v1/appointments/admin/bookings")
                                .header("Authorization", "Bearer " + patientToken))
                                .andExpect(status().isForbidden());
        }

        @Test
        void getMonthlySchedule_Admin_Success() throws Exception {
                mockMvc.perform(get("/api/v1/appointments/admin/bookings/monthly")
                                .header("Authorization", "Bearer " + adminToken)
                                .param("from", "2024-01-01")
                                .param("to", "2024-01-31"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data").exists());
        }

        @Test
        void searchAdminBookings_Success() throws Exception {
                mockMvc.perform(get("/api/v1/appointments/admin/bookings/search")
                                .header("Authorization", "Bearer " + adminToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data").exists());
        }

        @Test
        void getMyAppointmentsStats_Success() throws Exception {
                mockMvc.perform(get("/api/v1/appointments/me/stats")
                                .header("Authorization", "Bearer " + patientToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.total").exists())
                                .andExpect(jsonPath("$.data.completed").exists())
                                .andExpect(jsonPath("$.data.pending").exists())
                                .andExpect(jsonPath("$.data.cancelled").exists());
        }
}
