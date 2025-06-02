package com.cleanengine.coin.realitybot.domain;

import com.cleanengine.coin.trade.entity.Trade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PlatformVWAPStateTest {

/*    @DisplayName("10개 이상의 거래를 보낼 경우 최신 10개만 계산하는 지")
    @Test
    void testVWAPStacksOver10Trades(){
        String ticker = "BTC";
        VWAPState vwapState = new VWAPState(ticker);
        for (int i = 1; i < 16; i++) {
            double price = i*100;
            System.out.println(price);
            vwapState.recordTrade(price,1);
        }
        System.out.println(vwapState.getTotalPriceVolume());
        System.out.println(vwapState.getTotalVolume());
        System.out.println(vwapState.getVWAP());
        assertEquals(vwapState.getVWAP(), 0);
    }*/

    @DisplayName("10개 이상의 모든 거래를 계산한다.")
    @Test
    void TestcalculateVWAPbyTrades() {
        String ticker = "BTC";
        PlatformVWAPState platformVwapState = new PlatformVWAPState(ticker);
        List<Trade> trades = List.of(
                new Trade(1, "BTC", LocalDateTime.now(), 2, 1, 10000.0, 10.0), // 100000
                new Trade(2, "BTC", LocalDateTime.now(), 2, 1, 11000.0, 10.0), // 110000
                new Trade(3, "BTC", LocalDateTime.now(), 2, 1, 12000.0, 10.0), // 120000
                new Trade(4, "BTC", LocalDateTime.now(), 2, 1, 13000.0, 10.0), // 130000
                new Trade(5, "BTC", LocalDateTime.now(), 2, 1, 14000.0, 10.0), // 140000
                new Trade(6, "BTC", LocalDateTime.now(), 2, 1, 15000.0, 10.0), // 150000
                new Trade(7, "BTC", LocalDateTime.now(), 2, 1, 16000.0, 10.0), // 160000
                new Trade(8, "BTC", LocalDateTime.now(), 2, 1, 17000.0, 10.0), // 170000
                new Trade(9, "BTC", LocalDateTime.now(), 2, 1, 18000.0, 10.0), // 180000
                new Trade(10, "BTC", LocalDateTime.now(), 2, 1, 19000.0, 10.0), // 190000
                new Trade(11, "BTC", LocalDateTime.now(), 2, 1, 20000.0, 10.0)  // 200000
        );
        platformVwapState.addTrades(trades);
        System.out.println(platformVwapState.getVWAP());
        assertEquals(platformVwapState.getVWAP(),15000.0);
    }
}