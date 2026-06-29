package com.medibook.modules.reporting.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class MonthlyReportRequest {

    @Min(value = 2000)
    private Integer year;

    @Min(value = 1)
    @Max(value = 12)
    private Integer month;
}
