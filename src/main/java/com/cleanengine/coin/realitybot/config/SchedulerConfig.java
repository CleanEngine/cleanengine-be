package com.cleanengine.coin.realitybot.config;

import com.cleanengine.coin.realitybot.api.ApiScheduler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.time.Duration;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class SchedulerConfig implements SchedulingConfigurer {

    //멀티쓰레드 환경 x
//    @Autowired
//    private TaskScheduler apiScheduler;
    private final ApiScheduler apiScheduler;

    @Value("${bot-handler.fixed-rate}")
    private Duration fixedRate;

    @Override
    public void configureTasks(ScheduledTaskRegistrar registrar) {
//        registrar.setScheduler(apiScheduler); //멀티 쓰레드 x
        registrar.addFixedRateTask(() -> {
            try {
                apiScheduler.MarketAllRequest();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, fixedRate);
    }
}
