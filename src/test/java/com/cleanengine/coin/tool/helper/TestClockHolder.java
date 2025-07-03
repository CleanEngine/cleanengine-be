package com.cleanengine.coin.tool.helper;

import com.cleanengine.coin.common.time.ClockHolder;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class TestClockHolder implements ClockHolder {
    private long timeMillis = 0L;
    private long callingGetTimeMillisCount = 0L;

    public TestClockHolder(long initialTimeMillis) {
        this.timeMillis = initialTimeMillis;
    }

    @Override
    public long getTimeMillis() {
        callingGetTimeMillisCount++;
        return timeMillis;
    }

    @Override
    public LocalDateTime convertTimeMillisToDateTime(long timeMillis) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timeMillis), ZoneId.systemDefault());
    }

    public void elapseTimeMillis(long millis){
        timeMillis += millis;
    }

    public long getCallingGetTimeMillisCount() {
        return callingGetTimeMillisCount;
    }
}
