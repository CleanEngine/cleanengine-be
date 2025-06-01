package com.cleanengine.coin.realitybot.vo;

import com.cleanengine.coin.trade.entity.Trade;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Getter
@Setter
@Slf4j
public class VWAPState {

    public VWAPState(String ticker) {
        this.ticker = ticker;
    }

    private String ticker;
//    private final Queue<Vwap> tradeQueue = new LinkedList<>(); //테스트를 위한 큐 -> 체결 db에서 데이터 조회
//    private int maxQueueSize = 10;

    private double totalPriceVolume = 0;
    private double totalVolume = 0;
    private double vwap = 0;


        //이건 처음에나 필요했지 queue나 10개씩 받아오면서 필요 없는 로직이 되어버림
    public void recordTrade(double price, double volume) {
//        tradeQueue.offer(new Vwap(price, volume)); //오로지 계산에만 목적을 둠
        totalPriceVolume += price * volume;
        totalVolume += volume;
    }

    public double getVWAP() {
            vwap = totalVolume == 0 ? 0.0 : totalPriceVolume / totalVolume;
        return vwap;
    }

    public void calculateVWAPbyTrades(List<Trade> trades) {
        for (Trade trade : trades) {
            double price = trade.getPrice();
            double volume = trade.getSize();
            recordTrade(price,volume);
        }
        getVWAP();
    }
}
