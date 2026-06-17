package com.medibook.modules.specialty.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SpecialtyRequest {

    @NotBlank
    @Size(max = 100)
    private String name;

    private String description;

}
