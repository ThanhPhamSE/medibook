package com.medibook.common.validation.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.medibook.common.validation.validator.FullNameValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Constraint(validatedBy = FullNameValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface FullName {

    String message() default "Fullname must contain only letters and spaces";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
