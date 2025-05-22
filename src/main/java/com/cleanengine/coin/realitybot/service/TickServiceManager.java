package com.cleanengine.coin.realitybot.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TickServiceManager {
    private final Map<String, ApiVWAPService> tickServiceMap = new ConcurrentHashMap<>();
    public ApiVWAPService getService(String ticker) {
        return tickServiceMap.computeIfAbsent(ticker, t -> new ApiVWAPService());
    }
}
