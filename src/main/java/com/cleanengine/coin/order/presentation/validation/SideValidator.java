package com.cleanengine.coin.order.presentation.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SideValidator implements ConstraintValidator<Side, String> {
    private static final String BID = "bid";
    private static final String ASK = "ask";

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        return s!=null && (s.equals(BID) || s.equals(ASK));
    }
}
