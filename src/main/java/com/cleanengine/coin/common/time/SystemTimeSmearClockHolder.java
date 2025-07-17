package com.cleanengine.coin.common.time;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

public class SystemTimeSmearClockHolder implements ClockHolder {
    private final ClockHolder clockHolder = new SystemDefaultClockHolder();
    private final AtomicLong atomicLastTimestampMillis = new AtomicLong(clockHolder.getTimeMillis());

    @Override
    public long getTimeMillis() {
        long currentTimeMillis = clockHolder.getTimeMillis();

        long lastTimestampMillis = atomicLastTimestampMillis.get();

        if(currentTimeMillis < lastTimestampMillis) {
            currentTimeMillis = lastTimestampMillis;
        }

        atomicLastTimestampMillis.compareAndSet(lastTimestampMillis, currentTimeMillis);

        return currentTimeMillis;
    }

    @Override
    public LocalDateTime convertTimeMillisToDateTime(long timeMillis) {
        return clockHolder.convertTimeMillisToDateTime(timeMillis);
    }
}
