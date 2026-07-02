package com.medibook.modules.user.dto.request;

import com.medibook.common.enums.Gender;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserSearchRequest {

    @Size(max = 100, message = "Keyword must not exceed 100 characters")
    private String keyword;

    @Positive(message = "Role id must be positive")
    private Long roleId;

    private Boolean isActive;

    private Gender gender;

}
