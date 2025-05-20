package com.cleanengine.coin.realitybot.vo;

import com.cleanengine.coin.trade.entity.Trade;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

@Getter
@Setter
public class VWAPState {

    public VWAPState(String ticker) {
        this.ticker = ticker;
    }

    private String ticker;
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
    }

    public double getVWAP() {
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
        return getVWAP();
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
