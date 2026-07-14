package com.medibook.modules.schedule.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medibook.common.enums.DayOfWeekEnum;
import com.medibook.modules.doctor.entity.Doctor;
import com.medibook.modules.doctor.repository.DoctorRepository;
import com.medibook.modules.schedule.dto.request.TimeOffRequest;
import com.medibook.modules.schedule.dto.request.WorkingPatternRequest;
import com.medibook.modules.schedule.entity.DoctorWorkingPattern;
import com.medibook.modules.schedule.repository.DoctorWorkingPatternRepository;
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

import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ScheduleControllerIntegrationTest {

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
    private DoctorWorkingPatternRepository workingPatternRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String doctorToken;
    private String adminToken;
    private Long doctorId;
    private LocalDateTime futureTime;

    @BeforeEach
    void setUp() throws Exception {
        Role doctorRole = roleRepository.findByName("DOCTOR").orElseThrow();
        Role adminRole = roleRepository.findByName("ADMIN").orElseThrow();

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
        doctor.setConsultationFee(java.math.BigDecimal.valueOf(100));
        doctor.setBiography("Experienced doctor");
        doctor.setSpecialty(specialty);
        doctor = doctorRepository.save(doctor);
        doctorId = doctor.getId();

        futureTime = LocalDateTime.now().plusDays(2).withHour(10).withMinute(0).withSecond(0).withNano(0);

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
    void createWorkingPattern_Success() throws Exception {
        WorkingPatternRequest request = new WorkingPatternRequest();
        request.setDoctorId(doctorId);
        request.setDayOfWeek(DayOfWeekEnum.MON);
        request.setStartTime(LocalTime.of(9, 0));
        request.setEndTime(LocalTime.of(17, 0));
        request.setSlotDuration(30);
        request.setBufferDuration(5);

        mockMvc.perform(post("/api/v1/schedules/working-patterns")
                .header("Authorization", "Bearer " + doctorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Created successful"))
                .andExpect(jsonPath("$.data.id").exists());
    }

    @Test
    void createWorkingPattern_Forbidden() throws Exception {
        WorkingPatternRequest request = new WorkingPatternRequest();
        request.setDoctorId(doctorId);
        request.setDayOfWeek(DayOfWeekEnum.MON);
        request.setStartTime(LocalTime.of(9, 0));
        request.setEndTime(LocalTime.of(17, 0));

        mockMvc.perform(post("/api/v1/schedules/working-patterns")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createWorkingPattern_Unauthorized() throws Exception {
        WorkingPatternRequest request = new WorkingPatternRequest();
        request.setDoctorId(doctorId);
        request.setDayOfWeek(DayOfWeekEnum.MON);

        mockMvc.perform(post("/api/v1/schedules/working-patterns")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateTimeOff_Success() throws Exception {
        TimeOffRequest request = new TimeOffRequest();
        request.setDoctorId(doctorId);
        request.setStartDatetime(futureTime);
        request.setEndDatetime(futureTime.plusHours(2));
        request.setReason("Personal leave");

        mockMvc.perform(post("/api/v1/schedules/time-offs")
                .header("Authorization", "Bearer " + doctorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Created successful"));
    }

    @Test
    void getDoctorSchedule_Success() throws Exception {
        mockMvc.perform(get("/api/v1/schedules/doctor/" + doctorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.doctorId").value(doctorId))
                .andExpect(jsonPath("$.data.workingPartterns").exists())
                .andExpect(jsonPath("$.data.timeOffs").exists());
    }

    @Test
    void generateSlots_Success() throws Exception {
        mockMvc.perform(post("/api/v1/schedules/slots")
                .param("doctorId", doctorId.toString())
                .param("date", futureTime.toLocalDate().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    void generateSlots_InvalidDate() throws Exception {
        mockMvc.perform(post("/api/v1/schedules/slots")
                .param("doctorId", doctorId.toString())
                .param("date", "invalid-date"))
                .andExpect(status().isBadRequest());
    }
}
