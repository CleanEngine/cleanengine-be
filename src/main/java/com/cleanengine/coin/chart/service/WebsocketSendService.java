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


    //직전 데이터등락율 보내는 메세지 형식
    public void sendChangeRate(Object data, String ticker) {
        log.debug("티커 {} 실시간 구독 요청", ticker);

        // 티커별 토픽으로 전송
        messagingTemplate.convertAndSend(
                "/topic/realTimeTradeRate/" + ticker, data);
        log.debug("전송 완료: /topic/realTimeTradeRate/{} -> {}", ticker, data);
    }

    //전날 종가 변동률로 보내는 메세지 형식
    public void sendPrevRate(Object data, String ticker) {
        log.debug("티커 {} 전일 대비 변동률 보내는 요청", ticker);
        messagingTemplate.convertAndSend(
                "/topic/prevRate/" + ticker, data);
        log.debug("전송 완료: /topic/prevRate/{} -> {}", ticker, data);
    }
}
