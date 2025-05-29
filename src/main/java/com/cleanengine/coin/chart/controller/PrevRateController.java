package com.cleanengine.coin.chart.controller;

import com.cleanengine.coin.chart.service.ChartSubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class PrevRateController {

    private final ChartSubscriptionService chartSubscriptionService;

    @MessageMapping("/subscribe/prevRate/{ticker}")
    public void subscribePrevRate(@DestinationVariable String ticker) {
        chartSubscriptionService.subscribePrevRate(ticker);
    }
}