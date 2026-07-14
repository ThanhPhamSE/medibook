package com.medibook.modules.user.dto.response;

import java.time.LocalDate;

import com.medibook.common.enums.Gender;

import lombok.Data;

@Data
public class UserResponse {

    private Long id;

    private String email;

    private String fullName;

    private String phone;

    private Gender gender;

    private LocalDate birthDate;

    private String profileImage;

    private Boolean isActive;

    private Long roleId;

    private String roleName;

    private Long doctorId;

}
