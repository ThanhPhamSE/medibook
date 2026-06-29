package com.medibook.modules.reporting.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.medibook.common.enums.AppointmentStatus;
import com.medibook.common.exception.BadRequestException;
import com.medibook.common.exception.ResourceNotFoundException;
import com.medibook.modules.appointment.facade.AppointmentReportingFacade;
import com.medibook.modules.doctor.facade.DoctorFacade;
import com.medibook.modules.reporting.dto.response.AppointmentStatisticResponse;
import com.medibook.modules.reporting.dto.response.DoctorPerformanceResponse;
import com.medibook.modules.reporting.dto.response.RevenueStatisticResponse;
import com.medibook.modules.reporting.service.ReportService;
import com.medibook.modules.review.facade.ReviewReportingFacade;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

        private final AppointmentReportingFacade appointmentReportingFacade;

        private final ReviewReportingFacade reviewReportingFacade;

        private final DoctorFacade doctorFacade;

        @Override
        public AppointmentStatisticResponse daily(LocalDate date) {

                LocalDateTime from = date.atStartOfDay();

                LocalDateTime to = date.plusDays(1).atStartOfDay();

                return buildStatistic(from, to);

        }

        @Override
        public AppointmentStatisticResponse monthly(int year, int month) {

                LocalDate first = LocalDate.of(year, month, 1);

                return buildStatistic(first.atStartOfDay(), first.plusMonths(1).atStartOfDay());

        }

        private AppointmentStatisticResponse buildStatistic(
                        LocalDateTime from,
                        LocalDateTime to) {

                return AppointmentStatisticResponse.builder()

                                .total(appointmentReportingFacade.countAppointments(from,
                                                to))

                                .pending(appointmentReportingFacade
                                                .countAppointmentsByStatus(
                                                                AppointmentStatus.PENDING, from, to))

                                .confirmed(appointmentReportingFacade
                                                .countAppointmentsByStatus(
                                                                AppointmentStatus.CONFIRMED, from, to))

                                .completed(appointmentReportingFacade
                                                .countAppointmentsByStatus(
                                                                AppointmentStatus.COMPLETED, from, to))

                                .cancelled(appointmentReportingFacade
                                                .countAppointmentsByStatus(
                                                                AppointmentStatus.CANCELLED, from, to))

                                .noShow(appointmentReportingFacade
                                                .countAppointmentsByStatus(
                                                                AppointmentStatus.NO_SHOW, from, to))

                                .build();

        }

        @Override
        public DoctorPerformanceResponse doctor(Long doctorId) {

                if (!doctorFacade.exists(doctorId)) {
                        throw new ResourceNotFoundException("Doctor not found");
                }

                Double avg = reviewReportingFacade.getAverageRating(doctorId);

                return DoctorPerformanceResponse.builder().doctorId(doctorId)
                                .completedAppointments(
                                                appointmentReportingFacade.countCompletedDoctor(doctorId))
                                .revenue(appointmentReportingFacade.sumDoctorRevenue(doctorId))
                                .averageRating(avg == null ? 0.0 : avg)
                                .build();
        }

        @Override
        public RevenueStatisticResponse revenue(LocalDate from, LocalDate to) {

                if (from.isAfter(to)) {
                        throw new BadRequestException("From date must be before or equal to To date");
                }

                return RevenueStatisticResponse.builder()
                                .totalAppointments(appointmentReportingFacade
                                                .countCompletedAppointments(from, to))
                                .totalRevenue(appointmentReportingFacade.sumRevenue(from, to))
                                .build();
        }

}
