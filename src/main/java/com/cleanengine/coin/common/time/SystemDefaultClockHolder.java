package com.cleanengine.coin.common.time;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class SystemDefaultClockHolder implements ClockHolder {
    // currentTimeMillis는 UTC 기준이다.
    @Override
    public long getTimeMillis() {
        return System.currentTimeMillis();
    }

    @Override
    public LocalDateTime convertTimeMillisToDateTime(long timeMillis) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timeMillis), ZoneId.systemDefault());
    }
}
