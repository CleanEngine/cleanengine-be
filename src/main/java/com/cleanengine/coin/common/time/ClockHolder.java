package com.cleanengine.coin.common.time;

import java.time.LocalDateTime;

public interface ClockHolder {
    long getTimeMillis();

    LocalDateTime convertTimeMillisToDateTime(long timeMillis);
}
