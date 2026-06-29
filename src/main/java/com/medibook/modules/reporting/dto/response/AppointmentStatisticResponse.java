package com.medibook.modules.reporting.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentStatisticResponse {

    private long total;

    private long pending;

    private long confirmed;

    private long completed;

    private long cancelled;

    private long noShow;

}
