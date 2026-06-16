package com.medibook.common.validation.validator;

import com.medibook.common.validation.annotation.VietnamPhone;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class VietnamPhoneValidator implements ConstraintValidator<VietnamPhone, String> {

    private static final String VN_PHONE_REGEX = "^(0[3|5|7|8|9])[0-9]{8}$";

    public boolean isValid(String value, ConstraintValidatorContext context) {

        if (value == null) {
            return false;
        }

        return value.matches(VN_PHONE_REGEX);
    }

}
