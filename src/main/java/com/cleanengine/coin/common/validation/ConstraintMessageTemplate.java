package com.cleanengine.coin.common.validation;

public final class ConstraintMessageTemplate {
    private ConstraintMessageTemplate() {}
    public static final String NOTBLANK_MESSAGE_TEMPLATE =
            " cannot be blank.";
    public static final String NOTNULL_MESSAGE_TEMPLATE =
            " cannot be null.";
    public static final String MIN_MESSAGE_TEMPLATE =
            " should be {value} or more. but, submitted value '${validatedValue}' is less than {value}.";
    public static final String MAX_MESSAGE_TEMPLATE =
            " should be {value} or less. but, submitted value '${validatedValue}' is more than {value}.";
    public static final String SIZE_MESSAGE_TEMPLATE =
            "'s length should be between {min} and {max}. but, submitted value was '${validatedValue}'.";
    public static final String POSITIVE_MESSAGE_TEMPLATE =
            " should be positive. but, submitted value '${validatedValue}' was negative.";
}