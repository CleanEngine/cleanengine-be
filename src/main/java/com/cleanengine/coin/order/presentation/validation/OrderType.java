package com.cleanengine.coin.order.presentation.validation;

import jakarta.validation.Constraint;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = OrderTypeValidator.class)
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface OrderType {
    String message() default "orderType must be 'market' or 'limit'";
    Class<?>[] groups() default {};
    Class<? extends Class<?>>[] payload() default {};
}
