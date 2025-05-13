package com.cleanengine.coin.realitybot.service;

import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.Queue;

@Service
public class PlatformVWAPService {//TODO 가상 시장 조회용 사라질 예정임
    private final Queue<Trade> tradeQueue = new LinkedList<>(); //테스트를 위한 큐 -> 체결 db에서 데이터 조회
    private int maxQueueSize = 5;

    private double totalPriceVolume = 0;
    private double totalVolume = 0;

    public void recordTrade(double price, double volume) {
        if (tradeQueue.size() >= maxQueueSize) {
            Trade removed = tradeQueue.poll();
            totalPriceVolume -= removed.price * removed.volume;
            totalVolume -= removed.volume;
        }

        tradeQueue.offer(new Trade(price, volume));
        totalPriceVolume += price * volume;
        totalVolume += volume;
        System.out.println("=== 최신 체결 큐 확인 : " + tradeQueue.peek().toString());
        System.out.println("=== platformVWAP 변동 : "+getPlatformVWAP());
    }

    public double getPlatformVWAP() {
        return totalVolume == 0 ? 0.0 : totalPriceVolume / totalVolume;
    }

    private static class Trade {
        double price;
        double volume;

        public Trade(double price, double volume) {
            this.price = price;
            this.volume = volume;
        }

        @Override
        public String toString() {
            return "Trade{" +
                    "price=" + price +
                    ", volume=" + volume +
                    '}';
        }
    }
}

