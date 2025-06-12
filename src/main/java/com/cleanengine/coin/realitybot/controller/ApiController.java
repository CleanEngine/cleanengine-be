package com.cleanengine.coin.realitybot.controller;

import com.cleanengine.coin.realitybot.api.ApiScheduler;
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

    @GetMapping("/{tickerName}")
    public ResponseEntity<?> getApiData(@PathVariable String tickerName) {
        String upperTickerName = tickerName.toUpperCase();
        System.out.println(upperTickerName);
        apiScheduler.MarketDataRequest(upperTickerName);
        return ResponseEntity.ok("Triggered marketAllRequest");
    }

    @GetMapping("/all")
    public ResponseEntity<?> getApiData() throws InterruptedException {
        apiScheduler.MarketAllRequest();
        return ResponseEntity.ok("Triggered marketAllRequest");
    }
}
