package com.cleanengine.coin.chart.controller;

import com.cleanengine.coin.common.annotation.WorkingServerProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@WorkingServerProfile
@RequiredArgsConstructor
public class RealTimeRateScheduler {
    private final RealTimeTradeController realTimeTradeController;

    @Scheduled(fixedDelay = 5000)
    public void sendPrevRate(){
        realTimeTradeController.realTimeTradeRate("TRUMP");
    }
}
