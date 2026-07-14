package com.medibook.modules.reporting.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.medibook.common.constant.RoleConstants;
import com.medibook.common.enums.AppointmentStatus;
import com.medibook.common.exception.BadRequestException;
import com.medibook.common.exception.ResourceNotFoundException;
import com.medibook.modules.appointment.facade.AppointmentReportingFacade;
import com.medibook.modules.appointment.repository.StatusCountProjection;
import com.medibook.modules.doctor.facade.DoctorFacade;
import com.medibook.modules.reporting.dto.response.AdminDashboardResponse;
import com.medibook.modules.reporting.dto.response.AppointmentStatisticResponse;
import com.medibook.modules.reporting.dto.response.AppointmentTrendResponse;
import com.medibook.modules.reporting.dto.response.DashboardStatsResponse;
import com.medibook.modules.reporting.dto.response.DoctorPerformanceResponse;
import com.medibook.modules.reporting.dto.response.RevenueStatisticResponse;
import com.medibook.modules.reporting.dto.response.RevenueTrendResponse;
import com.medibook.modules.reporting.dto.response.SpecialtyDistributionResponse;
import com.medibook.modules.reporting.dto.response.TopDoctorResponse;
import com.medibook.modules.reporting.service.ReportService;
import com.medibook.modules.review.facade.ReviewReportingFacade;
import com.medibook.modules.user.repository.RoleRepository;
import com.medibook.modules.user.repository.UserRepository;

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
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

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

    private AppointmentStatisticResponse buildStatistic(LocalDateTime from, LocalDateTime to) {
        Map<AppointmentStatus, Long> counts = appointmentReportingFacade.countGroupByStatus(from, to)
                .stream()
                .collect(Collectors.toMap(
                        StatusCountProjection::getStatus,
                        StatusCountProjection::getCnt,
                        Long::sum)); // merge duplicate keys by summing

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

    @Override
    public List<AppointmentTrendResponse> appointmentTrend(LocalDate from, LocalDate to) {

        if (from.isAfter(to)) {
            throw new BadRequestException("From date must be before to date");
        }

        return appointmentReportingFacade.getAppointmentTrend(from.atStartOfDay(), to.plusDays(1).atStartOfDay());
    }

    @Override
    public List<RevenueTrendResponse> revenueTrend(LocalDate from, LocalDate to) {
        log.debug("Generating revenue trend: from={}, to={}", from, to);

        if (from.isAfter(to)) {
            throw new BadRequestException("From date must be before or equal to To date");
        }

        return appointmentReportingFacade.getRevenueTrend(from, to);
    }

    @Override
    public DashboardStatsResponse dashboardStats(LocalDate from, LocalDate to) {
        log.debug("Generating dashboard stats: from={}, to={}", from, to);

        long totalPatients = roleRepository.findByName(RoleConstants.CUSTOMER)
                .map(role -> userRepository.countByRoleIdAndDeletedAtIsNull(role.getId()))
                .orElse(0L);

        long totalDoctors = roleRepository.findByName(RoleConstants.DOCTOR)
                .map(role -> userRepository.countByRoleIdAndDeletedAtIsNull(role.getId()))
                .orElse(0L);

        Long totalAppointments = appointmentReportingFacade.countCompletedAppointments(from, to);
        java.math.BigDecimal totalRevenue = appointmentReportingFacade.sumRevenue(from, to);

        return DashboardStatsResponse.builder()
                .totalPatients(totalPatients)
                .totalDoctors(totalDoctors)
                .totalAppointments(totalAppointments != null ? totalAppointments : 0L)
                .totalRevenue(totalRevenue != null ? totalRevenue : java.math.BigDecimal.ZERO)
                .build();
    }

    @Override
    public AppointmentStatisticResponse range(LocalDate from, LocalDate to) {
        log.debug("Generating appointment range stats: from={}, to={}", from, to);

        if (from.isAfter(to)) {
            throw new BadRequestException("From date must be before or equal to To date");
        }

        return buildStatistic(from.atStartOfDay(), to.plusDays(1).atStartOfDay());
    }

    @Override
    public AdminDashboardResponse adminDashboard(LocalDate from, LocalDate to) {
        log.debug("Generating admin dashboard: from={}, to={}", from, to);

        if (from.isAfter(to)) {
            throw new BadRequestException("From date must be before or equal to To date");
        }

        // Dashboard stats
        long totalPatients = roleRepository.findByName(RoleConstants.CUSTOMER)
                .map(role -> userRepository.countByRoleIdAndDeletedAtIsNull(role.getId()))
                .orElse(0L);

        long totalDoctors = roleRepository.findByName(RoleConstants.DOCTOR)
                .map(role -> userRepository.countByRoleIdAndDeletedAtIsNull(role.getId()))
                .orElse(0L);

        Long totalAppointments = appointmentReportingFacade.countCompletedAppointments(from, to);
        java.math.BigDecimal totalRevenue = appointmentReportingFacade.sumRevenue(from, to);

        // Trends
        List<AppointmentTrendResponse> appointmentTrend = appointmentReportingFacade.getAppointmentTrend(from.atStartOfDay(), to.plusDays(1).atStartOfDay());
        List<RevenueTrendResponse> revenueTrend = appointmentReportingFacade.getRevenueTrend(from, to);

        // Specialty distribution - get from specialties with doctor count
        List<SpecialtyDistributionResponse> specialtyDistribution = appointmentReportingFacade.getSpecialtyDistribution()
                .stream()
                .map(sd -> SpecialtyDistributionResponse.builder()
                        .label(sd.getLabel())
                        .value(sd.getValue())
                        .build())
                .collect(java.util.stream.Collectors.toList());

        // Top doctors - get from doctor performance
        List<TopDoctorResponse> topDoctors = appointmentReportingFacade.getTopDoctors()
                .stream()
                .map(td -> TopDoctorResponse.builder()
                        .label(td.getLabel())
                        .value(td.getValue())
                        .build())
                .collect(java.util.stream.Collectors.toList());

        return AdminDashboardResponse.builder()
                .totalPatients(totalPatients)
                .totalDoctors(totalDoctors)
                .totalAppointments(totalAppointments != null ? totalAppointments : 0L)
                .totalRevenue(totalRevenue != null ? totalRevenue : java.math.BigDecimal.ZERO)
                .appointmentTrend(appointmentTrend)
                .revenueTrend(revenueTrend)
                .specialtyDistribution(specialtyDistribution)
                .topDoctors(topDoctors)
                .build();
    }
}
