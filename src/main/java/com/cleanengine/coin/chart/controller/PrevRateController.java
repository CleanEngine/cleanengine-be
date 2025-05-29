package com.cleanengine.coin.chart.controller;

import com.cleanengine.coin.chart.dto.PrevRateDto;
import com.cleanengine.coin.chart.service.ChartSubscriptionService;
import com.cleanengine.coin.chart.service.RealTimeDataPrevRateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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