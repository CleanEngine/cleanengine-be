package com.cleanengine.coin.realitybot.config;

import com.cleanengine.coin.realitybot.api.ApiScheduler;
import com.cleanengine.coin.realitybot.api.RateOfExchangeRefresher;
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
    private final RateOfExchangeRefresher rateOfExchangeRefresher;
    @Value("${bot-handler.cron}")
    private final String cron;
    @Value("${bot-handler.fixed-rate}")
    private final Duration fixedRate;
    @Value("${bot-handler.exchange-rate}")
    private final Duration exchangeRate;

    protected SchedulerConfig(ApiScheduler apiScheduler, @Value("${bot-handler.fixed-rate}") Duration fixedRate, UnitPriceRefresher unitPriceRefresher,
                              @Value("${bot-handler.cron}")String cron, RateOfExchangeRefresher rateOfExchangeRefresher,
                              @Value("${bot-handler.exchange-rate}") Duration exchangeRate) {
        this.apiScheduler = apiScheduler;
        this.fixedRate = fixedRate;
        this.unitPriceRefresher = unitPriceRefresher;
        this.cron = cron;
        this.rateOfExchangeRefresher = rateOfExchangeRefresher;
        this.exchangeRate = exchangeRate;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar registrar) {
        unitPriceRefresher.initializeUnitPrices(); //선반영
        rateOfExchangeRefresher.exchangeRate();

        registrar.addCronTask(() -> {
            try {
                unitPriceRefresher.initializeUnitPrices();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, cron);
        registrar.addFixedRateTask(() -> {
            try {
//                apiScheduler.getMarketAllRequest();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, fixedRate);
        registrar.addFixedRateTask(() -> {
            try {
                rateOfExchangeRefresher.exchangeRate();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, exchangeRate);
    }
}
