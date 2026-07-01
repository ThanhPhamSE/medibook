package com.medibook.modules.medicalrecord.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import com.medibook.modules.medicalrecord.dto.request.MedicalRecordCreateRequest;
import com.medibook.modules.medicalrecord.dto.request.MedicalRecordUpdateRequest;
import com.medibook.modules.medicalrecord.dto.response.MedicalRecordResponse;
import com.medibook.modules.medicalrecord.entity.MedicalRecord;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface MedicalRecordMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "appointment", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    MedicalRecord toEntity(MedicalRecordCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "appointment", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntity(MedicalRecordUpdateRequest request, @MappingTarget MedicalRecord entity);

    @Mapping(target = "appointmentId", source = "appointment.id")
    @Mapping(target = "bookingCode", source = "appointment.bookingCode")
    @Mapping(target = "doctorId", source = "appointment.doctor.id")
    @Mapping(target = "doctorName", source = "appointment.doctor.user.fullName")
    @Mapping(target = "patientId", source = "appointment.patient.id")
    @Mapping(target = "patientName", source = "appointment.patient.fullName")
    MedicalRecordResponse toResponse(MedicalRecord entity);

}
