package com.cleanengine.coin.chart.controller;

import com.cleanengine.coin.chart.dto.RealTimeDataDto;
import com.cleanengine.coin.chart.service.ChartSubscriptionService;
import com.cleanengine.coin.chart.service.RealTimeTradeService;
import com.cleanengine.coin.chart.service.WebsocketSendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class RealTimeTradeController {

    private final ChartSubscriptionService chartSubscriptionService;

    /**
     * 클라이언트가 /app/subscribe/realTimeTradeRate/{ticker} 로 send() 하면
     * {ticker} 값을 받습니다.
     */
    @MessageMapping("/subscribe/realTimeTradeRate/{ticker}")
    public void realTimeTradeRate(@DestinationVariable String ticker) {
        chartSubscriptionService.subscribeRealTimeTradeRate(ticker);
    }
}