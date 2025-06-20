package com.cleanengine.coin.chart.controller;

import com.cleanengine.coin.chart.dto.RealTimeOhlcDto;
import com.cleanengine.coin.chart.service.minute.PagingMinuteOhlcDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/minute-ohlc")
@RequiredArgsConstructor
public class MinuteOhlcDataController {

    private final PagingMinuteOhlcDataService service;

    /**
     * GET /api/minute-ohlc?ticker=BTC&count=100&interval=1&from=2025-06-19T10:30
     * DB에 있는 과거 거래를 interval 단위로 묶어 OHLC+volume을 계산한 리스트 반환
     */
    @GetMapping
    public ResponseEntity<List<RealTimeOhlcDto>> getMinuteOhlc(
            @RequestParam("ticker") String ticker,
            @RequestParam(value = "count", defaultValue = "100") int count,
            @RequestParam(value = "interval", defaultValue = "1") int interval,
            @RequestParam(value = "from", required = false) LocalDateTime from
    ) {
        if (from == null) {
            from = LocalDateTime.now();
        }

        List<RealTimeOhlcDto> data = service.getMinuteOhlcData(ticker, count, interval, from.minusMinutes(1));
        return ResponseEntity.ok(data);
    }

}