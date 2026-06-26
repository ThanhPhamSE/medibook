package com.medibook.modules.user.dto.request;

import com.medibook.common.enums.Gender;

import lombok.Data;

@Data
public class UserSearchRequest {

    private String keyword;

    private Long roleId;

    private Boolean isActive;

    private Gender gender;

}
