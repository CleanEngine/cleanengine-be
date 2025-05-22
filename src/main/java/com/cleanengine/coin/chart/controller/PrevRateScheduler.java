package com.cleanengine.coin.chart.controller;

import com.cleanengine.coin.common.annotation.WorkingServerProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@WorkingServerProfile
@RequiredArgsConstructor
public class PrevRateScheduler {
    private final PrevRateController prevRateController;

    @Scheduled(fixedDelay = 1000)
    public void sendPrevRate(){
        prevRateController.subscribePrevRate("TRUMP");
    }
}
