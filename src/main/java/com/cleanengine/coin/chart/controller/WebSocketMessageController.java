package com.cleanengine.coin.chart.controller;

import com.cleanengine.coin.chart.dto.RealTimeOhlcDto;
import com.cleanengine.coin.chart.service.ChartSubscriptionService;
import com.cleanengine.coin.chart.service.RealTimeOhlcService;
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
    private final RealTimeOhlcService realTimeOhlcService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChartDataController chartDataController;

    /**
     * 실시간 OHLC 데이터 구독 처리
     */
    @MessageMapping("/subscribe/realTimeOhlc")
    public void subscribeRealTimeOhlc(RealTimeTradeMappingDto request) {
        String ticker = request.getTicker();
        log.debug("실시간 OHLC 데이터 구독 요청: {}", ticker);

        // 구독 목록에 추가
        subscriptionService.subscribeRealTimeOhlc(ticker);

        // 구독 즉시 최근 실시간 OHLC 데이터 전송
        RealTimeOhlcDto latestOhlcData = realTimeOhlcService.getRealTimeOhlc(ticker);

        RealTimeOhlcDto lastSentData = chartDataController.getLastSentOhlcDataMap().get(ticker);

        if (latestOhlcData == null) {
            if (lastSentData != null) {
                // 이전에 전송한 데이터가 있으면 재사용
                log.debug("티커 {}의 실시간 OHLC 데이터가 없습니다. 이전 데이터 재사용", ticker);
                messagingTemplate.convertAndSend("/topic/realTimeOhlc/" + ticker, lastSentData);
            } else {
                // 이전 데이터도 없는 경우 빈 데이터 전송
                log.debug("티커 {}의 실시간 OHLC 데이터가 없습니다. 빈 데이터 전송", ticker);
                RealTimeOhlcDto emptyData = createEmptyRealTimeOhlcDto(ticker);
                messagingTemplate.convertAndSend("/topic/realTimeOhlc/" + ticker, emptyData);
                // 빈 데이터도 캐시에 저장
                chartDataController.getLastSentOhlcDataMap().put(ticker, emptyData);
            }
        } else {
            log.debug("티커 {}의 실시간 OHLC 데이터 전송: {}", ticker, latestOhlcData);
            messagingTemplate.convertAndSend("/topic/realTimeOhlc/" + ticker, latestOhlcData);
            // 데이터 캐시에 저장
            chartDataController.getLastSentOhlcDataMap().put(ticker, latestOhlcData);

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