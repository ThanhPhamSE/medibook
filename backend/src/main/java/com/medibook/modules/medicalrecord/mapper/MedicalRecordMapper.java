package com.medibook.modules.medicalrecord.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.medibook.modules.medicalrecord.dto.response.MedicalRecordResponse;
import com.medibook.modules.medicalrecord.entity.MedicalRecord;

@Mapper(componentModel = "spring")
public interface MedicalRecordMapper {

    @Mapping(target = "appointmentId", source = "appointment.id")
    @Mapping(target = "bookingCode", source = "appointment.bookingCode")
    @Mapping(target = "doctorId", source = "appointment.doctor.id")
    @Mapping(target = "doctorName", source = "appointment.doctor.user.fullName")
    @Mapping(target = "patientId", source = "appointment.patient.id")
    @Mapping(target = "patientName", source = "appointment.patient.fullName")
    MedicalRecordResponse toResponse(MedicalRecord entity);

}
