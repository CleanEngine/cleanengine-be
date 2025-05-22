package com.cleanengine.coin.chart.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RealTimeRateScheduler {
    private final RealTimeTradeController realTimeTradeController;


    //todo: 임시적으로 트리거를 만든 부분 추후에 삭제될 예정
    @Scheduled(fixedDelay = 5000)
    public void sendPrevRate(){
        realTimeTradeController.realTimeTradeRate("TRUMP");
    }
}
