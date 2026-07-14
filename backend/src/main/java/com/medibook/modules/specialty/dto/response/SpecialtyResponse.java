package com.medibook.modules.specialty.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecialtyResponse {

    private Long id;
    private String name;
    private String description;
    private Integer doctorCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
