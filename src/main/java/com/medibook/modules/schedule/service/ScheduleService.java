package com.medibook.modules.schedule.service;

import java.util.List;

import com.medibook.modules.schedule.dto.request.SlotGenerateRequest;
import com.medibook.modules.schedule.dto.request.TimeOffRequest;
import com.medibook.modules.schedule.dto.request.WorkingPatternRequest;
import com.medibook.modules.schedule.dto.response.DoctorScheduleResponse;
import com.medibook.modules.schedule.dto.response.SlotResponse;
import com.medibook.modules.schedule.dto.response.TimeOffResponse;
import com.medibook.modules.schedule.dto.response.WorkingPatternResponse;

public interface ScheduleService {

    WorkingPatternResponse createWorkingPattern(WorkingPatternRequest request);

    void deleteWorkingPattern(Long id);

    TimeOffResponse createTimeOff(TimeOffRequest request);

    void deleteTimeOff(Long id);

    List<SlotResponse> generateSlots(SlotGenerateRequest request);

    DoctorScheduleResponse getDoctorSchedule(Long doctorId);
}
