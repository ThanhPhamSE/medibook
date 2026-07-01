package com.medibook.modules.appointment.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.medibook.common.enums.AppointmentStatus;
import com.medibook.common.response.PageResponse;
import com.medibook.modules.appointment.dto.request.AppointmentCreateRequest;
import com.medibook.modules.appointment.dto.request.AppointmentRescheduleRequest;
import com.medibook.modules.appointment.dto.response.AppointmentResponse;
import com.medibook.modules.appointment.dto.response.BookedSlotResponse;
import com.medibook.modules.appointment.entity.Appointment;

public interface AppointmentService {

        AppointmentResponse createAppointment(AppointmentCreateRequest request);

        AppointmentResponse getAppointment(Long id);

        Page<AppointmentResponse> getMyAppointments(Pageable pageable);

        void cancelAppointment(Long id, String reason);

        boolean isSlotBooked(Long doctorId, LocalDateTime startDatetime);

        List<BookedSlotResponse> getBookedAppointmentsByDate(Long doctorId, LocalDate date);

        void confirmAppointment(Long id);

        void completeAppointment(Long id);

        void markNoShow(Long id);

        Page<AppointmentResponse> getDoctorAppointments(AppointmentStatus status, LocalDate from, LocalDate to,
                        Pageable pageable);

        AppointmentResponse rescheduleAppointment(Long appointmentId, AppointmentRescheduleRequest request);

        Appointment getAppointmentEntity(Long id);

        PageResponse<AppointmentResponse> getAllBookings(Pageable pageable);

        PageResponse<AppointmentResponse> getMonthlySchedule(Long doctorId, AppointmentStatus status, LocalDate from,
                        LocalDate to, Pageable pageable);

        PageResponse<AppointmentResponse> searchAdminBookings(String bookingCode, Long doctorId, Long patientId,
                        AppointmentStatus status, LocalDate from, LocalDate to, Pageable pageable);

        Page<AppointmentResponse> getTodayAppointments(Pageable pageable);

        Page<AppointmentResponse> getCurrentWeekAppointments(Pageable pageable);
}
