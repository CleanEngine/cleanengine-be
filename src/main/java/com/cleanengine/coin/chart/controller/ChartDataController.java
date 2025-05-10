package com.cleanengine.coin.chart.controller;


import com.cleanengine.coin.chart.Dto.CandleDto;
import com.cleanengine.coin.chart.service.ChartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChartDataController {
    private final ChartService chartService;
    private final SimpMessagingTemplate messagingTemplate;

    @Scheduled(cron = "0 * * * * *") // 매 분 0초에 실행
    public void publishMinuteCandles() {
        LocalDateTime end = LocalDateTime.now().withSecond(0).withNano(0);
        LocalDateTime start = end.minusMinutes(1);

        log.info("Fetching candle data from {} to {}", start, end);

        List<CandleDto> candles = chartService.getMinuteCandles(start, end);

        for (CandleDto candle : candles) {
            String destination = "/topic/candles/" + candle.getTicker();
            messagingTemplate.convertAndSend(destination, candle);
            log.debug("Sent candle for {} at {}", candle.getTicker(), candle.getTimestamp());
        }
    }


}
