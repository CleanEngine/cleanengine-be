package com.cleanengine.coin.realitybot.vo;

import com.cleanengine.coin.trade.entity.Trade;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class VWAPStateTest {


    @Test
    void testVWAPwithSingleTrade() {
        String ticker = "BTC";
        VWAPState vwapState = new VWAPState(ticker);
        vwapState.recordTrade(18000.0,10.0);
        assertEquals(vwapState.getTotalPriceVolume(), 180000.0);
        assertEquals(vwapState.getTotalVolume(), 10.0);
        assertEquals(vwapState.getVWAP(), 18000.0);
    }
    @Test
    void testVWAPwith0VolumeTrade() {
        String ticker = "BTC";
        VWAPState vwapState = new VWAPState(ticker);
        vwapState.recordTrade(18000.0,0);
        assertEquals(vwapState.getTotalPriceVolume(), 0);
        assertEquals(vwapState.getTotalVolume(), 0);
        assertEquals(vwapState.getVWAP(), 0);
    }
    @Test
    void testVWAPwith0PriceTrade() {
        String ticker = "BTC";
        VWAPState vwapState = new VWAPState(ticker);
        vwapState.recordTrade(0,100);
        assertEquals(vwapState.getTotalPriceVolume(), 0);
        assertEquals(vwapState.getTotalVolume(), 100);
        assertEquals(vwapState.getVWAP(), 0);
    }
    @Test
    void testVWAPStackTrades(){
        String ticker = "BTC";
        VWAPState vwapState = new VWAPState(ticker);
        vwapState.recordTrade(18000,100);
        vwapState.recordTrade(17000,70);
        vwapState.recordTrade(16000,50);
        vwapState.recordTrade(15000,30);
        vwapState.recordTrade(14000,10);
        assertEquals(vwapState.getTotalPriceVolume(), 4380000.0);
        assertEquals(vwapState.getTotalVolume(), 260.0);
        assertEquals(vwapState.getVWAP(), 16846.1538,0.0001);
    }
    @Test
    void testVWAPStackTradesWith0Volumes(){
        String ticker = "BTC";
        VWAPState vwapState = new VWAPState(ticker);
        vwapState.recordTrade(18000,0);
        vwapState.recordTrade(17000,0);
        vwapState.recordTrade(16000,0);
        vwapState.recordTrade(15000,0);
        vwapState.recordTrade(14000,0);
        assertEquals(vwapState.getTotalPriceVolume(), 0);
        assertEquals(vwapState.getTotalVolume(), 0);
        assertEquals(vwapState.getVWAP(), 0.0);
    }
    @Test
    void testVWAPStack1Trades(){
        String ticker = "BTC";
        VWAPState vwapState = new VWAPState(ticker);
        vwapState.recordTrade(18000,0);
        vwapState.recordTrade(17000,0);
        vwapState.recordTrade(16000,0);
        vwapState.recordTrade(15000,0);
        vwapState.recordTrade(14000,1);
        assertEquals(vwapState.getTotalPriceVolume(), 14000.0);
        assertEquals(vwapState.getTotalVolume(), 1);
        assertEquals(vwapState.getVWAP(), 14000.0);
    }
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
    @Test
    void getVWAP() {
    }

    @Test
    void TestcalculateVWAPbyTrades() {
        String ticker = "BTC";
        VWAPState vwapState = new VWAPState(ticker);
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
        vwapState.calculateVWAPbyTrades(trades);
        System.out.println(vwapState.getVWAP());
        assertEquals(vwapState.getVWAP(),15000.0);
    }
}