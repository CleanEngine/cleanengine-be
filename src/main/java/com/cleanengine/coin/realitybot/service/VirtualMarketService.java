package com.cleanengine.coin.realitybot.service;

import com.cleanengine.coin.realitybot.dto.Ticks;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Queue;
import java.util.Random;

@Service
@Slf4j
public class VirtualMarketService {
    private final Random random = new Random();
    private final TickService tickService;
    public VirtualMarketService(TickService tickService) {
        this.tickService = tickService;
    }

    public double getVirtualMarketPrice(Queue<Ticks> ticksQueue) {
        double realVWAP = tickService.calculateVWAP(ticksQueue);
        double adjustment = getRandomAdjustment(realVWAP);
        double virtualPrice = realVWAP + adjustment;
        log.info("현실 VWAP : {}, 보정치 : {}, 가상 VWAP : {}", realVWAP, adjustment, virtualPrice); // (가상 = trade 체결금액에서 vwap)만 구하면 됨
        return virtualPrice;
    }

    private double getRandomAdjustment(double realVWAP) {
        double maxDeviationRate = 0.01;
        double deviation = realVWAP * maxDeviationRate;

        return (random.nextDouble()*2-1)* deviation;
    }
}
