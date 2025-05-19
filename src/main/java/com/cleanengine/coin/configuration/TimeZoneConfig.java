package com.cleanengine.coin.configuration;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.time.ZoneId;
import java.util.TimeZone;

@Configuration
public class TimeZoneConfig {
    public static final ZoneId SEOUL_ZONE_ID = ZoneId.of("Asia/Seoul");

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone(SEOUL_ZONE_ID));
    }
}
