package com.medibook.modules.specialty.mapper;

import org.springframework.stereotype.Component;

import com.medibook.modules.specialty.dto.response.SpecialtyResponse;
import com.medibook.modules.specialty.entity.Specialty;

@Component
public class SpecialtyMapper {

    public SpecialtyResponse toResponse(Specialty specialty) {

        return SpecialtyResponse.builder().id(specialty.getId()).name(specialty.getName())
                .description(specialty.getDescription()).createdAt(specialty.getCreatedAt()).build();
    }

}
