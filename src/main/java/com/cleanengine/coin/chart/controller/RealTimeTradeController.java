package com.cleanengine.coin.chart.controller;

import com.cleanengine.coin.chart.dto.RealTimeDataDto;
import com.cleanengine.coin.chart.service.RealTimeTradeService;
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

    private final SimpMessagingTemplate messagingTemplate;
    private final RealTimeTradeService realTimeTradeService;

    /**
     * 클라이언트가 /app/subscribe/realTimeTradeRate/{ticker} 로 send() 하면
     * {ticker} 값을 받습니다.
     */
    @MessageMapping("/subscribe/realTimeTradeRate/{ticker}")
    public void realTimeTradeRate(@DestinationVariable String ticker) {
        log.info("티커 {} 실시간 구독 요청", ticker);

        // 서비스로부터 DTO 생성
        RealTimeDataDto data = realTimeTradeService.generateRealTimeData(ticker);

        // 티커별 토픽으로 전송
        messagingTemplate.convertAndSend(
                "/topic/realTimeTradeRate/" + ticker,
                data
        );
        log.debug("전송 완료: /topic/realTimeTradeRate/{} -> {}", ticker, data);
    }
}