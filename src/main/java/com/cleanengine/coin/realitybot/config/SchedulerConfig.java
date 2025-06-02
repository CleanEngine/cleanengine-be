package com.cleanengine.coin.realitybot.config;

import com.cleanengine.coin.realitybot.api.ApiScheduler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.time.Duration;

@Configuration
@EnableScheduling
//@RequiredArgsConstructor
public class SchedulerConfig implements SchedulingConfigurer {

    //멀티쓰레드 환경 x
//    @Autowired
//    private TaskScheduler apiScheduler;
    private final ApiScheduler apiScheduler;
    @Value("${bot-handler.fixed-rate}")
    private final Duration fixedRate;

    protected SchedulerConfig(ApiScheduler apiScheduler, @Value("${bot-handler.fixed-rate}") Duration fixedRate) {
        this.apiScheduler = apiScheduler;
        this.fixedRate = fixedRate;
    }

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
