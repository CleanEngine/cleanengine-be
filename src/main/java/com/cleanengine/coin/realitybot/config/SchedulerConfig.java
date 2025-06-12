package com.cleanengine.coin.realitybot.config;

import com.cleanengine.coin.realitybot.api.ApiScheduler;
import com.cleanengine.coin.realitybot.api.UnitPriceRefresher;
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
    private final UnitPriceRefresher unitPriceRefresher;
    @Value("${bot-handler.cron}")
    private final String cron;
    @Value("${bot-handler.fixed-rate}")
    private final Duration fixedRate;

    protected SchedulerConfig(ApiScheduler apiScheduler, @Value("${bot-handler.fixed-rate}") Duration fixedRate, UnitPriceRefresher unitPriceRefresher,
                              @Value("${bot-handler.cron}")String cron) {
        this.apiScheduler = apiScheduler;
        this.fixedRate = fixedRate;
        this.unitPriceRefresher = unitPriceRefresher;
        this.cron = cron;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar registrar) {
        unitPriceRefresher.initializeUnitPrices(); //선반영

        registrar.addCronTask(() -> {
            try {
                unitPriceRefresher.initializeUnitPrices();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, cron);
        registrar.addFixedRateTask(() -> {
            try {
                apiScheduler.getMarketAllRequest();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, fixedRate);
    }
}
