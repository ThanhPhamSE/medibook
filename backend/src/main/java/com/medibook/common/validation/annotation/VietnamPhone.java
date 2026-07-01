package com.medibook.common.validation.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.medibook.common.validation.validator.VietnamPhoneValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Constraint(validatedBy = VietnamPhoneValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface VietnamPhone {

    String message() default "Invalid Vietnamese phone number";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
