package com.medibook.modules.appointment;

import com.medibook.common.enums.AppointmentStatus;
import com.medibook.common.enums.DayOfWeekEnum;
import com.medibook.common.exception.BadRequestException;
import com.medibook.modules.appointment.dto.request.AppointmentCreateRequest;
import com.medibook.modules.appointment.dto.response.AppointmentResponse;
import com.medibook.modules.appointment.entity.Appointment;
import com.medibook.modules.appointment.repository.AppointmentRepository;
import com.medibook.modules.appointment.service.AppointmentService;
import com.medibook.modules.doctor.entity.Doctor;
import com.medibook.modules.doctor.repository.DoctorRepository;
import com.medibook.modules.schedule.entity.DoctorWorkingPattern;
import com.medibook.modules.schedule.repository.DoctorWorkingPatternRepository;
import com.medibook.modules.specialty.entity.Specialty;
import com.medibook.modules.specialty.repository.SpecialtyRepository;
import com.medibook.modules.user.entity.Role;
import com.medibook.modules.user.entity.User;
import com.medibook.modules.user.repository.RoleRepository;
import com.medibook.modules.user.repository.UserRepository;
import com.medibook.security.model.CustomUserPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.mock.web.MockHttpServletRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class AppointmentConcurrencyTest {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private DoctorWorkingPatternRepository workingPatternRepository;

    @Autowired
    private SpecialtyRepository specialtyRepository;

    private Doctor testDoctor;
    private List<User> testPatients = new ArrayList<>();
    private LocalDateTime testSlotTime;

    @BeforeEach
    void setUp() {
        testSlotTime = LocalDateTime.now().plusDays(2).withHour(10).withMinute(0).withSecond(0).withNano(0);

        Role customerRole = roleRepository.findByName("CUSTOMER").orElseThrow();
        Role doctorRole = roleRepository.findByName("DOCTOR").orElseThrow();

        Specialty specialty = new Specialty();
        specialty.setName("Cardiology");
        specialty.setDescription("Heart specialists");
        specialty = specialtyRepository.save(specialty);

        testPatients.clear();
        for (int i = 1; i <= 5; i++) {
            User patient = new User();
            patient.setEmail("patient" + i + "@test.com");
            patient.setPassword("password");
            patient.setFullName("Patient " + i);
            patient.setRole(customerRole);
            patient.setIsActive(true);
            patient = userRepository.save(patient);
            testPatients.add(patient);
        }

        User doctorUser = new User();
        doctorUser.setEmail("doctor@test.com");
        doctorUser.setPassword("password");
        doctorUser.setFullName("Dr. Doctor");
        doctorUser.setRole(doctorRole);
        doctorUser.setIsActive(true);
        doctorUser = userRepository.save(doctorUser);

        testDoctor = new Doctor();
        testDoctor.setConsultationFee(BigDecimal.valueOf(100));
        testDoctor.setUser(doctorUser);
        testDoctor.setSpecialty(specialty);
        testDoctor = doctorRepository.save(testDoctor);

        DoctorWorkingPattern pattern = new DoctorWorkingPattern();
        pattern.setDoctor(testDoctor);
        pattern.setDayOfWeek(DayOfWeekEnum.fromJavaDayOfWeek(testSlotTime.getDayOfWeek()));
        pattern.setStartTime(LocalTime.of(9, 0));
        pattern.setEndTime(LocalTime.of(18, 0));
        pattern.setSlotDuration(30);
        pattern.setBufferDuration(0);
        workingPatternRepository.save(pattern);

        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        appointmentRepository.deleteAll();
        workingPatternRepository.deleteAll();
        doctorRepository.deleteAll();
        userRepository.deleteAll();
        specialtyRepository.deleteAll();
    }

    private void setSecurityContext(User user) {
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));

        CustomUserPrincipal principal = new CustomUserPrincipal(
            user.getId(),
            user.getEmail(),
            user.getPassword(),
            user.getIsActive(),
            user.getRole().getName(),
            List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().getName()))
        );
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void testConcurrentAppointmentBooking_SameSlot_ShouldPreventDoubleBooking() throws InterruptedException {
        int threadCount = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        List<Exception> exceptions = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            final int patientIndex = i;
            final User patient = testPatients.get(patientIndex);
            executorService.submit(() -> {
                try {
                    startLatch.await();

                    setSecurityContext(patient);

                    AppointmentCreateRequest request = new AppointmentCreateRequest();
                    request.setDoctorId(testDoctor.getId());
                    request.setStartDateTime(testSlotTime);
                    request.setNote("Concurrent test " + patientIndex);

                    AppointmentResponse response = appointmentService.createAppointment(request);
                    if (response != null) {
                        successCount.incrementAndGet();
                    }
                } catch (BadRequestException e) {
                    if (e.getMessage().contains("Slot already booked") || e.getMessage().contains("Could not acquire slot lock")) {
                        failureCount.incrementAndGet();
                    } else {
                        exceptions.add(e);
                    }
                } catch (Exception e) {
                    exceptions.add(e);
                } finally {
                    SecurityContextHolder.clearContext();
                    RequestContextHolder.resetRequestAttributes();
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        endLatch.await();
        executorService.shutdown();

        assertThat(exceptions).isEmpty();
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failureCount.get()).isEqualTo(threadCount - 1);

        List<Appointment> appointments = appointmentRepository.findAll();
        assertThat(appointments).hasSize(1);
        assertThat(appointments.get(0).getDoctor().getId()).isEqualTo(testDoctor.getId());
        assertThat(appointments.get(0).getStartDatetime()).isEqualTo(testSlotTime);
    }

    @Test
    void testConcurrentAppointmentBooking_DifferentSlots_ShouldAllSucceed() throws InterruptedException {
        int threadCount = 3;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        List<Exception> exceptions = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            final int hourOffset = i;
            final User patient = testPatients.get(hourOffset);
            executorService.submit(() -> {
                try {
                    startLatch.await();

                    setSecurityContext(patient);

                    LocalDateTime slotTime = testSlotTime.plusHours(hourOffset);

                    AppointmentCreateRequest request = new AppointmentCreateRequest();
                    request.setDoctorId(testDoctor.getId());
                    request.setStartDateTime(slotTime);
                    request.setNote("Different slot test " + hourOffset);

                    AppointmentResponse response = appointmentService.createAppointment(request);
                    if (response != null) {
                        successCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    exceptions.add(e);
                } finally {
                    SecurityContextHolder.clearContext();
                    RequestContextHolder.resetRequestAttributes();
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        endLatch.await();
        executorService.shutdown();

        assertThat(exceptions).isEmpty();
        assertThat(successCount.get()).isEqualTo(threadCount);

        List<Appointment> appointments = appointmentRepository.findAll();
        assertThat(appointments).hasSize(threadCount);
    }

    @Test
    void testPessimisticLock_PreventsPhantomReads() throws InterruptedException {
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(2);

        executorService.submit(() -> {
            try {
                startLatch.await();

                setSecurityContext(testPatients.get(0));

                AppointmentCreateRequest request = new AppointmentCreateRequest();
                request.setDoctorId(testDoctor.getId());
                request.setStartDateTime(testSlotTime);
                request.setNote("First thread");

                appointmentService.createAppointment(request);
                successCount.incrementAndGet();
            } catch (BadRequestException e) {
                if (e.getMessage().contains("Slot already booked") || e.getMessage().contains("Could not acquire slot lock")) {
                    failureCount.incrementAndGet();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                SecurityContextHolder.clearContext();
                RequestContextHolder.resetRequestAttributes();
                endLatch.countDown();
            }
        });

        executorService.submit(() -> {
            try {
                Thread.sleep(50);
                startLatch.await();

                setSecurityContext(testPatients.get(1));

                AppointmentCreateRequest request = new AppointmentCreateRequest();
                request.setDoctorId(testDoctor.getId());
                request.setStartDateTime(testSlotTime);
                request.setNote("Second thread");

                appointmentService.createAppointment(request);
                successCount.incrementAndGet();
            } catch (BadRequestException e) {
                if (e.getMessage().contains("Slot already booked") || e.getMessage().contains("Could not acquire slot lock")) {
                    failureCount.incrementAndGet();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                SecurityContextHolder.clearContext();
                RequestContextHolder.resetRequestAttributes();
                endLatch.countDown();
            }
        });

        startLatch.countDown();
        endLatch.await();
        executorService.shutdown();

        assertThat(successCount.get() + failureCount.get()).isEqualTo(2);
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failureCount.get()).isEqualTo(1);
    }
}
