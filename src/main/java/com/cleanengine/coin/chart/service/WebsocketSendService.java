package com.cleanengine.coin.chart.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class WebsocketSendService {
    private final SimpMessagingTemplate messagingTemplate;


    //전송만해
    public void send(Object data, String ticker){
        log.debug("티커 {} 실시간 구독 요청", ticker);

        // 티커별 토픽으로 전송
        messagingTemplate.convertAndSend(
                "/topic/realTimeTradeRate/" + ticker,
                data
        );
        log.debug("전송 완료: /topic/realTimeTradeRate/{} -> {}", ticker, data);
    }
}
