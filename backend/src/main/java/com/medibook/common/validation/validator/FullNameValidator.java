package com.medibook.common.validation.validator;

import com.medibook.common.validation.annotation.FullName;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class FullNameValidator implements ConstraintValidator<FullName, String> {

    private static final String NAME_REGEX = "^[\\p{L} ]{2,100}$";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {

            return false;

        }

        return value.trim().matches(NAME_REGEX);
    }

}
