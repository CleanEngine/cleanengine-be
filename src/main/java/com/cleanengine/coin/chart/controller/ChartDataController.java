package com.cleanengine.coin.chart.controller;


import com.cleanengine.coin.chart.dto.RealTimeOhlcDto;
import com.cleanengine.coin.chart.service.*;
import com.cleanengine.coin.chart.service.ChartSubscriptionService;
import com.cleanengine.coin.chart.service.RealTimeOhlcService;
import com.cleanengine.coin.common.annotation.WorkingServerProfile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@WorkingServerProfile
@RequiredArgsConstructor
@Slf4j
public class ChartDataController {

    private final ChartSubscriptionService subscriptionService;
    private final SimpMessagingTemplate messagingTemplate;
    private final RealTimeOhlcService realTimeOhlcService;
    // 티커별 마지막으로 전송한 OHLC 데이터 캐싱
    @Getter
    private final Map<String, RealTimeOhlcDto> lastSentOhlcDataMap = new ConcurrentHashMap<>();


    /**
     * 1초마다 실행 - 실시간 OHLC 데이터 전송
     */
    @Scheduled(fixedRate = 1000)
    public void publishRealTimeOhlc() {
        try {
            log.debug("△ 실시간 OHLC 데이터 스케줄러 실행");

            // 구독된 티커가 없으면 조기 종료
            if (subscriptionService.getAllRealTimeOhlcSubscribedTickers().isEmpty()) {
                log.debug("실시간 OHLC 구독된 티커 없음, 전송 생략");
                return;
            }

            // 모든 구독된 티커에 대해 데이터 전송
            for (String ticker : subscriptionService.getAllRealTimeOhlcSubscribedTickers()) {
                try {
                    log.debug("티커 {} 실시간 OHLC 데이터 전송 중...", ticker);

                    // 티커별 최신 OHLC 데이터 조회 및 전송
                    RealTimeOhlcDto ohlcData = realTimeOhlcService.getRealTimeOhlc(ticker);

                    if (ohlcData == null) {
                        // 이전에 전송한 데이터가 있는지 확인
                        RealTimeOhlcDto lastSentData = lastSentOhlcDataMap.get(ticker);

                        if (lastSentData != null) {
                            // 이전 데이터가 있으면 타임스탬프만 업데이트하여 재사용
                            log.debug("티커 {}의 실시간 OHLC 데이터가 없습니다. 이전 데이터 재사용", ticker);
                            RealTimeOhlcDto updatedData = new RealTimeOhlcDto(lastSentData.getTicker(), LocalDateTime.now(),  // 현재 시간으로 업데이트
                                    lastSentData.getOpen(), lastSentData.getHigh(), lastSentData.getLow(), lastSentData.getClose(), lastSentData.getVolume());

                            messagingTemplate.convertAndSend("/topic/realTimeOhlc/" + ticker, updatedData);
                            lastSentOhlcDataMap.put(ticker, updatedData);  // 캐시 업데이트
                        } else {
                            // 이전 데이터도 없는 경우 빈 데이터 전송 (첫 구독 시)
                            log.debug("티커 {}의 이전 OHLC 데이터도 없습니다. 빈 데이터 전송", ticker);
                            RealTimeOhlcDto emptyData = new RealTimeOhlcDto();
                            emptyData.setTicker(ticker);
                            emptyData.setTimestamp(LocalDateTime.now());
                            emptyData.setOpen(0.0);
                            emptyData.setHigh(0.0);
                            emptyData.setLow(0.0);
                            emptyData.setClose(0.0);
                            emptyData.setVolume(0.0);

                            messagingTemplate.convertAndSend("/topic/realTimeOhlc/" + ticker, emptyData);
                            lastSentOhlcDataMap.put(ticker, emptyData);  // 캐시 업데이트
                        }
                    } else {
                        // 조회된 실시간 OHLC 데이터 전송
                        messagingTemplate.convertAndSend("/topic/realTimeOhlc/" + ticker, ohlcData);
                        lastSentOhlcDataMap.put(ticker, ohlcData);  // 캐시 업데이트
                        log.debug("실시간 OHLC 데이터 전송: {}", ohlcData);
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