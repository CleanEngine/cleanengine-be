package com.cleanengine.coin.configuration;

import com.cleanengine.coin.common.time.ClockHolder;
import com.cleanengine.coin.common.time.SystemTimeSmearClockHolder;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.ZoneId;
import java.util.TimeZone;

@Configuration
public class TimeConfig {
    public static final ZoneId SEOUL_ZONE_ID = ZoneId.of("Asia/Seoul");

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone(SEOUL_ZONE_ID));
    }

    @Bean
    public ClockHolder clockHolder() {
        return new SystemTimeSmearClockHolder();
    }
}
