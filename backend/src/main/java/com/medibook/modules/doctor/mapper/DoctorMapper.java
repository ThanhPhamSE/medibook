package com.medibook.modules.doctor.mapper;

import java.util.List;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import com.medibook.modules.doctor.dto.request.CreateDoctorRequest;
import com.medibook.modules.doctor.dto.request.UpdateDoctorRequest;
import com.medibook.modules.doctor.dto.request.UpgradeToDoctorRequest;
import com.medibook.modules.doctor.dto.response.DoctorResponse;
import com.medibook.modules.doctor.dto.response.DoctorSummaryResponse;
import com.medibook.modules.doctor.entity.Doctor;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface DoctorMapper {

    // CREATE
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "specialty", ignore = true)
    @Mapping(target = "averageRating", ignore = true)
    @Mapping(target = "totalReviews", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    Doctor toEntity(CreateDoctorRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "specialty", ignore = true)
    @Mapping(target = "averageRating", ignore = true)
    @Mapping(target = "totalReviews", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    Doctor toEntity(UpgradeToDoctorRequest request);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    Doctor toSnapshot(Doctor doctor);

    // UPDATE
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "specialty", ignore = true)
    @Mapping(target = "averageRating", ignore = true)
    @Mapping(target = "totalReviews", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntity(UpdateDoctorRequest request, @MappingTarget Doctor doctor);

    // DETAIL RESPONSE
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.fullName", target = "fullName")
    @Mapping(source = "user.email", target = "email")
    @Mapping(source = "user.phone", target = "phone")
    @Mapping(source = "user.profileImage", target = "profileImage")
    @Mapping(source = "user.isActive", target = "active")
    @Mapping(source = "specialty.id", target = "specialtyId")
    @Mapping(source = "specialty.name", target = "specialtyName")
    DoctorResponse toResponse(Doctor doctor);

    List<DoctorResponse> toResponseList(List<Doctor> doctors);

    // SUMMARY RESPONSE
    @Mapping(source = "user.fullName", target = "fullName")
    @Mapping(source = "user.profileImage", target = "profileImage")
    @Mapping(source = "specialty.id", target = "specialtyId")
    @Mapping(source = "specialty.name", target = "specialtyName")
    @Mapping(source = "degree", target = "degree")
    @Mapping(source = "biography", target = "biography")
    DoctorSummaryResponse toSummaryResponse(Doctor doctor);

    List<DoctorSummaryResponse> toSummaryResponseList(List<Doctor> doctors);

}
