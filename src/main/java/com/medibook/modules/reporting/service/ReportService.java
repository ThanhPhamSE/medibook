package com.medibook.modules.reporting.service;

import java.time.LocalDate;

import com.medibook.modules.reporting.dto.response.AppointmentStatisticResponse;
import com.medibook.modules.reporting.dto.response.DoctorPerformanceResponse;
import com.medibook.modules.reporting.dto.response.RevenueStatisticResponse;

public interface ReportService {

    AppointmentStatisticResponse daily(LocalDate date);

    AppointmentStatisticResponse monthly(int year, int month);

    RevenueStatisticResponse revenue(LocalDate from,
            LocalDate to);

    DoctorPerformanceResponse doctor(Long doctorId);

}
