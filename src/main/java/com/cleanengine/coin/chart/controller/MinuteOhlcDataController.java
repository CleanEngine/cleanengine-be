package com.cleanengine.coin.chart.controller;

import com.cleanengine.coin.chart.dto.RealTimeOhlcDto;
import com.cleanengine.coin.chart.service.minute.MinuteOhlcDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/minute-ohlc")
@RequiredArgsConstructor
public class MinuteOhlcDataController {

    private final MinuteOhlcDataService service;

    /**
     * GET /api/minute-ohlc?ticker=BTC
     * DB에 있는 과거 거래를 1분 단위로 묶어 OHLC+volume을 계산한 리스트 반환
     */
    @GetMapping
    public ResponseEntity<List<RealTimeOhlcDto>> getMinuteOhlc(
            @RequestParam("ticker") String ticker
    ) {
        List<RealTimeOhlcDto> data = service.getMinuteOhlcData(ticker);
        return ResponseEntity.ok(data);
    }
}