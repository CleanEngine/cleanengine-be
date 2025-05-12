package com.cleanengine.coin.order.presentation.validation;

import jakarta.validation.Constraint;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = SideValidator.class)
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Side {
    String message() default "side must be 'bid' or 'ask'";
    Class<?>[] groups() default {};
    Class<? extends Class<?>>[] payload() default {};
}
