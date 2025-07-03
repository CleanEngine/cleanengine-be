package com.cleanengine.coin.common.time;

public class SystemDefaultClockHolder implements ClockHolder {
    @Override
    public long getTimeMillis() {
        return System.currentTimeMillis();
    }
}
