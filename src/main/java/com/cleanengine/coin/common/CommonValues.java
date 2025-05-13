package com.cleanengine.coin.common;

public abstract class CommonValues {
    public static final double MINIMUM_ORDER_SIZE = 0.00000001;

    public static boolean approxEquals(double a, double b) {
        return Math.abs(a - b) < MINIMUM_ORDER_SIZE;
    }
}
