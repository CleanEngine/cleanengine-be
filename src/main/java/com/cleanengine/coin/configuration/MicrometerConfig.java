package com.cleanengine.coin.configuration;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.config.MeterFilterReply;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("actuator")
@Configuration
public class MicrometerConfig {
    @Bean
    public MeterFilter meterFilter() {
        final String hibernateFilterPrefix = "hibernate.statements";

        return new MeterFilter() {
            @Override
            public MeterFilterReply accept(Meter.Id id) {
                if (id.getName().startsWith("hibernate.")) {
                    if(id.getName().startsWith(hibernateFilterPrefix)) {
                        return MeterFilterReply.ACCEPT;
                    }
                    else{
                        return MeterFilterReply.DENY;
                    }
                }
                return MeterFilterReply.NEUTRAL;
            }
        };
    }
}
