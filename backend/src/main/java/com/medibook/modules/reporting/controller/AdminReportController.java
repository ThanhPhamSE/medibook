package com.medibook.modules.reporting.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.medibook.common.response.ApiResponse;
import com.medibook.modules.reporting.dto.request.MonthlyReportRequest;
import com.medibook.modules.reporting.dto.request.RevenueStatisticRequest;
import com.medibook.modules.reporting.dto.response.AdminDashboardResponse;
import com.medibook.modules.reporting.dto.response.AppointmentStatisticResponse;
import com.medibook.modules.reporting.dto.response.AppointmentTrendResponse;
import com.medibook.modules.reporting.dto.response.DashboardStatsResponse;
import com.medibook.modules.reporting.dto.response.DoctorPerformanceResponse;
import com.medibook.modules.reporting.dto.response.RevenueStatisticResponse;
import com.medibook.modules.reporting.dto.response.RevenueTrendResponse;
import com.medibook.modules.reporting.service.ReportService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/reports")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasRole('ADMIN')")
public class AdminReportController {

    private final ReportService reportService;

    @GetMapping("/appointments/daily")
    public ResponseEntity<ApiResponse<AppointmentStatisticResponse>> daily(@RequestParam @NotNull LocalDate date) {

        return ResponseEntity.ok(ApiResponse.success(reportService.daily(date)));

    }

    @GetMapping("/appointments/monthly")
    public ResponseEntity<ApiResponse<AppointmentStatisticResponse>> monthly(

            @Valid @ModelAttribute MonthlyReportRequest request) {

        return ResponseEntity.ok(ApiResponse.success(reportService.monthly(request.getYear(), request.getMonth())));
    }

    @GetMapping("/revenue")
    public ResponseEntity<ApiResponse<RevenueStatisticResponse>> revenue(
            @Valid @ModelAttribute RevenueStatisticRequest request) {

        return ResponseEntity.ok(ApiResponse.success(reportService.revenue(request.getFrom(), request.getTo())));

    }

    @GetMapping("/doctors/{doctorId}")
    public ResponseEntity<ApiResponse<DoctorPerformanceResponse>> doctor(@PathVariable @Positive Long doctorId) {

        return ResponseEntity.ok(ApiResponse.success(reportService.doctor(doctorId)));

    }

    @GetMapping("/appointments/trend")
    public ResponseEntity<ApiResponse<List<AppointmentTrendResponse>>> appointmentTrend(
            @RequestParam LocalDate from,
            @RequestParam LocalDate to) {

        return ResponseEntity.ok(ApiResponse.success(reportService.appointmentTrend(from, to)));
    }

    @GetMapping("/revenue/trend")
    public ResponseEntity<ApiResponse<List<RevenueTrendResponse>>> revenueTrend(
            @RequestParam LocalDate from,
            @RequestParam LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success(reportService.revenueTrend(from, to)));
    }

    @GetMapping("/dashboard-stats")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> dashboardStats(
            @RequestParam LocalDate from,
            @RequestParam LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success(reportService.dashboardStats(from, to)));
    }

    @GetMapping("/appointments/range")
    public ResponseEntity<ApiResponse<AppointmentStatisticResponse>> appointmentRange(
            @RequestParam LocalDate from,
            @RequestParam LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success(reportService.range(from, to)));
    }

    @GetMapping("/admin-dashboard")
    public ResponseEntity<ApiResponse<AdminDashboardResponse>> adminDashboard(
            @RequestParam LocalDate from,
            @RequestParam LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success(reportService.adminDashboard(from, to)));
    }
}
