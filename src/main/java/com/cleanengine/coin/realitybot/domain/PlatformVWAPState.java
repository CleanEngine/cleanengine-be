package com.cleanengine.coin.realitybot.domain;

import com.cleanengine.coin.trade.domain.model.Trade;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Getter
@Setter
@Slf4j
public class PlatformVWAPState {

    public PlatformVWAPState(String ticker) {
        this.ticker = ticker;
    }

    private String ticker;
    private final VWAPCalculator calculator = new VWAPCalculator();
//    private final Queue<Vwap> tradeQueue = new LinkedList<>(); //테스트를 위한 큐 -> 체결 db에서 데이터 조회
//    private int maxQueueSize = 10;

    public void addTrades(List<Trade> trades) {
        for (Trade trade : trades) {
            double price = trade.getPrice();
            double volume = trade.getSize();
            calculator.recordTrade(price,volume);
        }
    }
    public double getVWAP(){
        return calculator.getVWAP();
    }}
