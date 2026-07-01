package com.medibook.modules.schedule.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.medibook.modules.schedule.dto.request.TimeOffRequest;
import com.medibook.modules.schedule.dto.request.WorkingPatternRequest;
import com.medibook.modules.schedule.dto.response.TimeOffResponse;
import com.medibook.modules.schedule.dto.response.WorkingPatternResponse;
import com.medibook.modules.schedule.entity.DoctorTimeOff;
import com.medibook.modules.schedule.entity.DoctorWorkingPattern;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ScheduleMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "doctor", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    DoctorWorkingPattern toEntity(WorkingPatternRequest request);

    @Mapping(target = "doctorId", source = "doctor.id")
    WorkingPatternResponse toResponse(DoctorWorkingPattern entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "doctor", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    DoctorTimeOff toEntity(TimeOffRequest request);

    @Mapping(target = "doctorId", source = "doctor.id")
    TimeOffResponse toResponse(DoctorTimeOff entity);
}