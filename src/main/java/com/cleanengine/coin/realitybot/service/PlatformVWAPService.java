package com.cleanengine.coin.realitybot.service;

import com.cleanengine.coin.realitybot.vo.VWAPState;
import com.cleanengine.coin.trade.entity.Trade;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class PlatformVWAPService {//TODO 가상 시장 조회용 사라질 예정임
//       private final VWAPState state = new VWAPState();
//
//
//    public void recordTrade(double price, double volume) {
//        state.recordTrade(price, volume);
//
//    }
//
//    public double getPlatformVWAP() {
//        return state.getVWAP();
//    }

    public double calculateVWAPbyTrades(String ticker,List<Trade> trades) {
        VWAPState state = new VWAPState(ticker);
        for (Trade trade : trades) {
            state.recordTrade(trade.getPrice(),trade.getSize());
        }
        log.info("VWAP by ticker: " + ticker + " state: " + state);
        return state.getVWAP();
    }


}

