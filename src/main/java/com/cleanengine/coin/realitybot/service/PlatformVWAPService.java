package com.cleanengine.coin.realitybot.service;

import com.cleanengine.coin.trade.entity.Trade;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

@Service
public class PlatformVWAPService {//TODO 가상 시장 조회용 사라질 예정임
    private final Queue<Vwap> tradeQueue = new LinkedList<>(); //테스트를 위한 큐 -> 체결 db에서 데이터 조회
    private int maxQueueSize = 10;

    private double totalPriceVolume = 0;
    private double totalVolume = 0;

    public void recordTrade(double price, double volume) {

        if (volume <= 0) return;

        if (tradeQueue.size() >= maxQueueSize) {
            Vwap removed = tradeQueue.poll();
            totalPriceVolume -= removed.price * removed.volume;
            totalVolume -= removed.volume;
        }

        tradeQueue.offer(new Vwap(price, volume));
        totalPriceVolume += price * volume;
        totalVolume += volume;
//        System.out.println("=== 최신 체결 큐 확인 : " + tradeQueue.peek().toString());
//        System.out.println("=== platformVWAP 변동 : "+getPlatformVWAP());
    }

    public double getPlatformVWAP() {
        return totalVolume == 0 ? 0.0 : totalPriceVolume / totalVolume;
    }

    public double calculateVWAPbyTrades(List<Trade> trades) {
        for (Trade trade : trades) {
            double price = trade.getPrice();
            double volume = trade.getSize();
            if (volume <= 0) continue;
            totalPriceVolume += price * volume;
            totalVolume += volume;

        }
        return getPlatformVWAP();
    }

    private static class Vwap { //원래 trade였는데 가상 계산 떄문에 냅두기
        double price;
        double volume;

        public Vwap(double price, double volume) {
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

