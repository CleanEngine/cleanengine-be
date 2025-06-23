package com.cleanengine.coin.chart.controller;

import com.cleanengine.coin.chart.dto.RealTimeOhlcDto;
import com.cleanengine.coin.chart.service.ChartSubscriptionService;
import com.cleanengine.coin.chart.service.RealTimeOhlcService; // RealTimeOhlcService 의존성은 이제 불필요
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketMessageController {

    private final ChartSubscriptionService subscriptionService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChartDataController chartDataController; // 이미 계산된 데이터를 가진 컨트롤러를 활용

    /**
     * 실시간 OHLC 데이터 구독 처리
     */
    @MessageMapping("/subscribe/realTimeOhlc")
    public void subscribeRealTimeOhlc(RealTimeTradeMappingDto request) {
        String ticker = request.getTicker();
        log.debug("실시간 OHLC 데이터 구독 요청: {}", ticker);

        // 구독 목록에 추가
        subscriptionService.subscribeRealTimeOhlc(ticker);

        RealTimeOhlcDto lastSentData = chartDataController.getLastSentOhlcDataMap().get(ticker);

        if (lastSentData == null) {
            log.debug("티커 {}의 캐시된 OHLC 데이터가 없습니다. 빈 데이터 전송", ticker);
            RealTimeOhlcDto emptyData = createEmptyRealTimeOhlcDto(ticker);
            messagingTemplate.convertAndSend("/topic/realTimeOhlc/" + ticker, emptyData);
        } else {
            // 캐시된 데이터가 있으면 즉시 전송
            log.debug("티커 {}의 캐시된 OHLC 데이터 즉시 전송: {}", ticker, lastSentData);
            messagingTemplate.convertAndSend("/topic/realTimeOhlc/" + ticker, lastSentData);
        }
    }

    private RealTimeOhlcDto createEmptyRealTimeOhlcDto(String ticker) {
        RealTimeOhlcDto emptyDto = new RealTimeOhlcDto();
        emptyDto.setTicker(ticker);
        emptyDto.setTimestamp(LocalDateTime.now());
        emptyDto.setOpen(0.0);
        emptyDto.setHigh(0.0);
        emptyDto.setLow(0.0);
        emptyDto.setClose(0.0);
        emptyDto.setVolume(0.0);
        return emptyDto;
    }

    /**
     * WebSocket 매핑용 DTO
     */
    @Setter
    @Getter
    public static class RealTimeTradeMappingDto {
        private String ticker;
    }
}