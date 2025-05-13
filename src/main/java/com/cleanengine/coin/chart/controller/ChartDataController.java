package com.cleanengine.coin.chart.controller;


import com.cleanengine.coin.chart.Dto.RealTimeOhlcDto;
//import com.cleanengine.coin.chart.Dto.RealTimeTradeDto;
import com.cleanengine.coin.chart.service.*;
import com.cleanengine.coin.chart.service.ChartSubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChartDataController {

    private final ChartSubscriptionService subscriptionService;
    private final SimpMessagingTemplate messagingTemplate;
    private final RealTimeOhlcService realTimeOhlcService;

    /**
     * 1초마다 실행 - 실시간 OHLC 데이터 전송
     */
    @Scheduled(fixedRate = 1000)
    public void publishRealTimeOhlc() {
        try {
            log.info("△ 실시간 OHLC 데이터 스케줄러 실행");

            // 구독된 티커가 없으면 조기 종료
            if (subscriptionService.getAllRealTimeOhlcSubscribedTickers().isEmpty()) {
                log.info("실시간 OHLC 구독된 티커 없음, 전송 생략");
                return;
            }

            // 모든 구독된 티커에 대해 데이터 전송
            for (String ticker : subscriptionService.getAllRealTimeOhlcSubscribedTickers()) {
                try {
                    log.info("티커 {} 실시간 OHLC 데이터 전송 중...", ticker);

                    // 티커별 최신 OHLC 데이터 조회 및 전송
                    RealTimeOhlcDto ohlcData = realTimeOhlcService.getRealTimeOhlc(ticker);

                    if (ohlcData == null) {
                        log.warn("티커 {}의 실시간 OHLC 데이터가 없습니다. 빈 데이터 전송", ticker);
                        RealTimeOhlcDto emptyData = new RealTimeOhlcDto();
                        emptyData.setTicker(ticker);
                        emptyData.setTimestamp(LocalDateTime.now());
                        emptyData.setOpen(0.0);
                        emptyData.setHigh(0.0);
                        emptyData.setLow(0.0);
                        emptyData.setClose(0.0);
                        emptyData.setVolume(0.0);

                        messagingTemplate.convertAndSend("/topic/realTimeOhlc/" + ticker, emptyData);
                    } else {
                        // 조회된 실시간 OHLC 데이터 전송
                        messagingTemplate.convertAndSend("/topic/realTimeOhlc/" + ticker, ohlcData);
                        log.info("실시간 OHLC 데이터 전송: {}", ohlcData);
                    }
                } catch (Exception e) {
                    log.error("티커 {} 실시간 OHLC 데이터 처리 중 오류: {}", ticker, e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            log.error("△ 실시간 OHLC 데이터 발행 중 오류: {}", e.getMessage(), e);
        }
    }


}