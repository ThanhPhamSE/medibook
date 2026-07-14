package com.medibook.modules.user.dto.request;

import java.time.LocalDate;

import com.medibook.common.enums.Gender;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @Size(max = 255)
    private String fullName;

    // Email cannot be changed after registration
    // @Email
    // @Size(max = 255)
    // private String email;

    @Size(max = 20)
    @Pattern(regexp = "^[0-9]{10,15}$", message = "Phone number must be 10-15 digits")
    private String phone;

    private Gender gender;

    private LocalDate birthDate;

    @Size(max = 500)
    private String profileImage;
}
