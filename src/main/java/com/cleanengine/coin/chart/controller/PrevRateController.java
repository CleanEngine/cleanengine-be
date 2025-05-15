package com.cleanengine.coin.chart.controller;

import com.cleanengine.coin.chart.dto.PrevRateDto;
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

    private final RealTimeDataPrevRateService prevRateService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/subscribe/prevRate/{ticker}")
    public void subscribePrevRate(@DestinationVariable String ticker) {
        log.info("티커 {} 전일 대비 변동률 구독 요청", ticker);

        // 서비스를 통해 전일 대비 변동률 데이터 얻기
        PrevRateDto data = prevRateService.generatePrevRateData(ticker);

        // 티커별 토픽으로 전송
        messagingTemplate.convertAndSend(
                "/topic/prevRate/" + ticker,
                data
        );
        log.debug("전송 완료: /topic/prevRate/{} -> {}", ticker, data);
    }
}