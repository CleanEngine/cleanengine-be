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

        String topic = buildTopic("realTimeTradeRate", ticker);
        sendMessage(topic, data);

        log.debug("전송 완료: {} -> {}", topic, data);
    }

    //전날 종가 변동률로 보내는 메세지 형식
    public void sendPrevRate(Object data, String ticker) {
        log.debug("티커 {} 전일 대비 변동률 보내는 요청", ticker);

        String topic = buildTopic("prevRate", ticker); // 기존 로직 유지
        sendMessage(topic, data);

        log.debug("전송 완료: {} -> {}", topic, data);
    }

    // 테스트하기 좋게 분리된 메서드들
    String buildTopic(String topicType, String ticker) {
        if (ticker == null || ticker.trim().isEmpty()) {
            throw new IllegalArgumentException("티커는 비어있을 수 없습니다");
        }
        return "/topic/" + topicType + "/" + ticker;
    }

    void sendMessage(String topic, Object data) {
        if (topic == null || topic.trim().isEmpty()) {
            throw new IllegalArgumentException("토픽은 비어있을 수 없습니다");
        }
        if (data == null) {
            throw new IllegalArgumentException("데이터는 null일 수 없습니다");
        }

        messagingTemplate.convertAndSend(topic, data);
    }
}