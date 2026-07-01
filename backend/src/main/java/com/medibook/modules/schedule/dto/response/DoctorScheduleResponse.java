package com.medibook.modules.schedule.dto.response;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DoctorScheduleResponse {

    private Long doctorId;

    private List<WorkingPatternResponse> workingPartterns;

    private List<TimeOffResponse> timeOffs;

}
