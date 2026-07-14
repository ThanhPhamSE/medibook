package com.medibook.modules.reporting.service;

import java.time.LocalDate;
import java.util.List;

import com.medibook.modules.reporting.dto.response.AdminDashboardResponse;
import com.medibook.modules.reporting.dto.response.AppointmentStatisticResponse;
import com.medibook.modules.reporting.dto.response.AppointmentTrendResponse;
import com.medibook.modules.reporting.dto.response.DashboardStatsResponse;
import com.medibook.modules.reporting.dto.response.DoctorPerformanceResponse;
import com.medibook.modules.reporting.dto.response.RevenueStatisticResponse;
import com.medibook.modules.reporting.dto.response.RevenueTrendResponse;

public interface ReportService {

    AppointmentStatisticResponse daily(LocalDate date);

    AppointmentStatisticResponse monthly(int year, int month);

    RevenueStatisticResponse revenue(LocalDate from,
            LocalDate to);

    DoctorPerformanceResponse doctor(Long doctorId);

    List<AppointmentTrendResponse> appointmentTrend(LocalDate from, LocalDate to);

    List<RevenueTrendResponse> revenueTrend(LocalDate from, LocalDate to);

    DashboardStatsResponse dashboardStats(LocalDate from, LocalDate to);

    AppointmentStatisticResponse range(LocalDate from, LocalDate to);

    AdminDashboardResponse adminDashboard(LocalDate from, LocalDate to);
}
