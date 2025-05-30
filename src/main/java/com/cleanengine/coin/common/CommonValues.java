package com.cleanengine.coin.common;

public abstract class CommonValues {
    public static final double MINIMUM_ORDER_SIZE = 0.00000001;
    public static final int SELL_ORDER_BOT_ID = 1;
    public static final int BUY_ORDER_BOT_ID = 2;
    public static final double INITIAL_USER_CASH = 50_000_000.0;

    public static boolean approxEquals(double a, double b) {
        return Math.abs(a - b) < MINIMUM_ORDER_SIZE;
    }
}
