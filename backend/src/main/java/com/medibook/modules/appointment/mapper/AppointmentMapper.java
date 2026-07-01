package com.medibook.modules.appointment.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.medibook.modules.appointment.dto.response.AppointmentResponse;
import com.medibook.modules.appointment.dto.response.BookedSlotResponse;
import com.medibook.modules.appointment.entity.Appointment;

@Mapper(componentModel = "spring")
public interface AppointmentMapper {

    @Mapping(target = "doctorId", source = "doctor.id")
    @Mapping(target = "doctorName", source = "doctor.user.fullName")
    @Mapping(target = "patientId", source = "patient.id")
    @Mapping(target = "patientName", source = "patient.fullName")
    @Mapping(target = "startDatetime", source = "startDatetime")
    @Mapping(target = "endDatetime", source = "endDatetime")
    AppointmentResponse toResponse(Appointment appointment);

    BookedSlotResponse toBookedSlot(Appointment appointment);

}
