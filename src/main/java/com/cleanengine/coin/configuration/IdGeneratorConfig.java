package com.cleanengine.coin.configuration;

import com.cleanengine.coin.common.idgenerator.LongIdGenerator;
import com.cleanengine.coin.common.idgenerator.LongSequenceSnowflakeIdGenerator;
import com.cleanengine.coin.common.time.ClockHolder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IdGeneratorConfig {
    @Value("${worker.id}")
    private Long workerId;

    @Bean
    public LongIdGenerator longIdGenerator(ClockHolder clockHolder) {
        return new LongSequenceSnowflakeIdGenerator(workerId, clockHolder);
    }
}
