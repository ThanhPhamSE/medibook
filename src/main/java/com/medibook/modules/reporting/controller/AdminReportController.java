package com.medibook.modules.reporting.controller;

import java.time.LocalDate;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.medibook.modules.reporting.dto.request.MonthlyReportRequest;
import com.medibook.modules.reporting.dto.request.RevenueStatisticRequest;
import com.medibook.modules.reporting.dto.response.AppointmentStatisticResponse;
import com.medibook.modules.reporting.dto.response.DoctorPerformanceResponse;
import com.medibook.modules.reporting.dto.response.RevenueStatisticResponse;
import com.medibook.modules.reporting.service.ReportService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
@Validated
public class AdminReportController {

    private final ReportService reportService;

    @GetMapping("/appointments/daily")
    public AppointmentStatisticResponse daily(@RequestParam @NotNull LocalDate date) {

        return reportService.daily(date);

    }

    @GetMapping("/appointments/monthly")
    public AppointmentStatisticResponse monthly(@Valid @ModelAttribute MonthlyReportRequest request) {

        return reportService.monthly(request.getYear(), request.getMonth());

    }

    @GetMapping("/revenue")
    public RevenueStatisticResponse revenue(@Valid @ModelAttribute RevenueStatisticRequest request) {

        return reportService.revenue(request.getFrom(), request.getTo());

    }

    @GetMapping("/doctors/{doctorId}")
    public DoctorPerformanceResponse doctor(@PathVariable @Positive Long doctorId) {

        return reportService.doctor(doctorId);

    }

}
