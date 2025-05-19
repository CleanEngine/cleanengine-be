package com.cleanengine.coin.order.presentation.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class OrderTypeValidator implements ConstraintValidator<OrderType, String> {
    private static final String MARKET = "market";
    private static final String LIMIT = "limit";

    @Override
    public void initialize(OrderType constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        return s!=null && (s.equals(MARKET) || s.equals(LIMIT));
    }
}
