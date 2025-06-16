package com.cleanengine.coin.realitybot.controller;

import com.cleanengine.coin.realitybot.api.ApiScheduler;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class ApiController {
    private final ApiScheduler apiScheduler;
    private final MeterRegistry meterRegistry;

    @GetMapping("/{tickerName}")
    public ResponseEntity<?> getApiData(@PathVariable String tickerName) {
        String upperTickerName = tickerName.toUpperCase();
        System.out.println(upperTickerName);
        apiScheduler.getMarketDataRequest(upperTickerName);
        return ResponseEntity.ok("Triggered marketAllRequest");
    }

    @GetMapping("/all")
    public ResponseEntity<?> getApiData() throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            long totalStart = System.currentTimeMillis();
            apiScheduler.getMarketAllRequest();
            long totalEnd = System.currentTimeMillis();
            System.out.printf("✅ 전체 처리시간: %d ms\n", (totalEnd - totalStart));
            meterRegistry.timer("api.market.total").record(totalEnd - totalStart, java.util.concurrent.TimeUnit.MILLISECONDS);
        }
        return ResponseEntity.ok("Triggered marketAllRequest");
    }
}
