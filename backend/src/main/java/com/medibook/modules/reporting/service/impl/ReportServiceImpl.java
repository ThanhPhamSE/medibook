package com.medibook.modules.reporting.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.medibook.common.enums.AppointmentStatus;
import com.medibook.common.exception.BadRequestException;
import com.medibook.common.exception.ResourceNotFoundException;
import com.medibook.modules.appointment.facade.AppointmentReportingFacade;
import com.medibook.modules.appointment.repository.StatusCountProjection;
import com.medibook.modules.doctor.facade.DoctorFacade;
import com.medibook.modules.reporting.dto.response.AppointmentStatisticResponse;
import com.medibook.modules.reporting.dto.response.DoctorPerformanceResponse;
import com.medibook.modules.reporting.dto.response.RevenueStatisticResponse;
import com.medibook.modules.reporting.service.ReportService;
import com.medibook.modules.review.facade.ReviewReportingFacade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private final AppointmentReportingFacade appointmentReportingFacade;
    private final ReviewReportingFacade reviewReportingFacade;
    private final DoctorFacade doctorFacade;

    @Override
    public AppointmentStatisticResponse daily(LocalDate date) {
        log.debug("Generating daily appointment stats for date: {}", date);
        LocalDateTime from = date.atStartOfDay();
        LocalDateTime to = date.plusDays(1).atStartOfDay();
        return buildStatistic(from, to);
    }

    @Override
    public AppointmentStatisticResponse monthly(int year, int month) {
        log.debug("Generating monthly appointment stats: year={}, month={}", year, month);
        LocalDate first = LocalDate.of(year, month, 1);
        return buildStatistic(first.atStartOfDay(), first.plusMonths(1).atStartOfDay());
    }

    /**
     * Lấy count theo từng status bằng 1 query GROUP BY thay vì 6 queries riêng lẻ.
     */
    private AppointmentStatisticResponse buildStatistic(LocalDateTime from, LocalDateTime to) {
        Map<AppointmentStatus, Long> counts = appointmentReportingFacade.countGroupByStatus(from, to)
                .stream()
                .collect(Collectors.toMap(StatusCountProjection::getStatus, StatusCountProjection::getCnt));

        long total = counts.values().stream().mapToLong(Long::longValue).sum();

        return AppointmentStatisticResponse.builder()
                .total(total)
                .pending(counts.getOrDefault(AppointmentStatus.PENDING, 0L))
                .confirmed(counts.getOrDefault(AppointmentStatus.CONFIRMED, 0L))
                .completed(counts.getOrDefault(AppointmentStatus.COMPLETED, 0L))
                .cancelled(counts.getOrDefault(AppointmentStatus.CANCELLED, 0L))
                .noShow(counts.getOrDefault(AppointmentStatus.NO_SHOW, 0L))
                .build();
    }

    @Override
    public DoctorPerformanceResponse doctor(Long doctorId) {
        log.debug("Generating performance report for doctorId: {}", doctorId);

        if (!doctorFacade.exists(doctorId)) {
            throw new ResourceNotFoundException("Doctor not found");
        }

        Double avg = reviewReportingFacade.getAverageRating(doctorId);

        return DoctorPerformanceResponse.builder()
                .doctorId(doctorId)
                .completedAppointments(appointmentReportingFacade.countCompletedDoctor(doctorId))
                .revenue(appointmentReportingFacade.sumDoctorRevenue(doctorId))
                .averageRating(avg == null ? 0.0 : avg)
                .build();
    }

    @Override
    public RevenueStatisticResponse revenue(LocalDate from, LocalDate to) {
        log.info("Generating revenue report: from={}, to={}", from, to);

        if (from.isAfter(to)) {
            throw new BadRequestException("From date must be before or equal to To date");
        }

        return RevenueStatisticResponse.builder()
                .totalAppointments(appointmentReportingFacade.countCompletedAppointments(from, to))
                .totalRevenue(appointmentReportingFacade.sumRevenue(from, to))
                .build();
    }
}
