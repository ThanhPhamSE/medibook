package com.medibook.modules.specialty.mapper;

import org.springframework.stereotype.Component;

import com.medibook.modules.specialty.dto.request.SpecialtyCreateRequest;
import com.medibook.modules.specialty.dto.request.SpecialtyUpdateRequest;
import com.medibook.modules.specialty.dto.response.SpecialtyResponse;
import com.medibook.modules.specialty.entity.Specialty;

@Component
public class SpecialtyMapper {

    public SpecialtyResponse toResponse(Specialty specialty) {

        return SpecialtyResponse.builder().id(specialty.getId()).name(specialty.getName())
                .description(specialty.getDescription()).createdAt(specialty.getCreatedAt())
                .updatedAt(specialty.getUpdatedAt()).build();

    }

    public Specialty toEntityWhenCreate(SpecialtyCreateRequest request) {

        Specialty specialty = new Specialty();

        specialty.setName(request.getName().trim());

        specialty.setDescription(request.getDescription().trim());

        return specialty;
    }

    public Specialty toEntityWhenUpdate(SpecialtyUpdateRequest request) {

        Specialty specialty = new Specialty();

        specialty.setName(request.getName());

        specialty.setDescription(request.getDescription());

        return specialty;
    }

}
