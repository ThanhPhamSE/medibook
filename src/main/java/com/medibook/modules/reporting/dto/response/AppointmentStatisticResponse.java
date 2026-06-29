package com.medibook.modules.reporting.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AppointmentStatisticResponse {

    private long total;

    private long pending;

    private long confirmed;

    private long completed;

    private long cancelled;

    private long noShow;

}
