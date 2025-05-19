package com.cleanengine.coin.chart.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RealTimeRateScheduler {
    private final RealTimeTradeController realTimeTradeController;

    @Scheduled(fixedDelay = 5000)
    public void sendPrevRate(){
        realTimeTradeController.realTimeTradeRate("TRUMP");
    }
}
