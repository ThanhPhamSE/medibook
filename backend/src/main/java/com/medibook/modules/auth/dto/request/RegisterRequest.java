package com.medibook.modules.auth.dto.request;

import java.time.LocalDate;

import com.medibook.common.enums.Gender;
import com.medibook.common.validation.annotation.FullName;
import com.medibook.common.validation.annotation.StrongPassword;
import com.medibook.common.validation.annotation.VietnamPhone;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email format is invalid")
    @Size(max = 255)
    private String email;

    @NotBlank
    @StrongPassword
    private String password;

    @NotBlank
    @FullName
    private String fullName;

    @NotBlank
    @VietnamPhone
    private String phone;

    @NotNull
    private Gender gender;

    @NotNull
    @Past
    private LocalDate birthDate;

    public void normalize() {
        if (this.email != null)
            this.email = this.email.trim().toLowerCase();

        if (this.fullName != null)
            this.fullName = this.fullName.trim().replaceAll("\\s+", " ");

        if (this.phone != null)
            this.phone = this.phone.trim();
    }
}
