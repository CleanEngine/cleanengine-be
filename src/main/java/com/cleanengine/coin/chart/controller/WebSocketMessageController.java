package com.cleanengine.coin.chart.controller;

import com.cleanengine.coin.chart.Dto.RealTimeOhlcDto;
import com.cleanengine.coin.chart.service.ChartSubscriptionService;
import com.cleanengine.coin.chart.service.RealTimeOhlcService;
import lombok.RequiredArgsConstructor;
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

    /**
     * 실시간 OHLC 데이터 구독 처리
     */
    @MessageMapping("/subscribe/realTimeOhlc")
    public void subscribeRealTimeOhlc(RealTimeTradeMappingDto request) {
        String ticker = request.getTicker();
        log.info("실시간 OHLC 데이터 구독 요청: {}", ticker);

        // 구독 목록에 추가
        subscriptionService.subscribeRealTimeOhlc(ticker);

        // 구독 즉시 최근 실시간 OHLC 데이터 전송
        RealTimeOhlcDto latestOhlcData = realTimeOhlcService.getRealTimeOhlc(ticker);

        if (latestOhlcData == null) {
            log.warn("티커 {}의 실시간 OHLC 데이터가 없습니다.", ticker);
            // 데이터가 없으면 빈 데이터 전송
            messagingTemplate.convertAndSend("/topic/realTimeOhlc/" + ticker, createEmptyRealTimeOhlcDto(ticker));
        } else {
            log.info("티커 {}의 실시간 OHLC 데이터 전송: {}", ticker, latestOhlcData);
            messagingTemplate.convertAndSend("/topic/realTimeOhlc/" + ticker, latestOhlcData);
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
    public static class RealTimeTradeMappingDto {
        private String ticker;

        public String getTicker() {
            return ticker;
        }

        public void setTicker(String ticker) {
            this.ticker = ticker;
        }
    }
}