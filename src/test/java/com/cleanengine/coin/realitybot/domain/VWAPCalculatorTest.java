package com.cleanengine.coin.realitybot.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class VWAPCalculatorTest {

    private final VWAPCalculator calculator = new VWAPCalculator();

    @DisplayName("trade 한 건으로 vwap 계산한다.")
    @Test
    void testVWAPwithSingleTrade() {

        calculator.recordTrade(18000.0,10.0);
        assertEquals(calculator.getTotalPriceVolume(), 180000.0);
        assertEquals(calculator.getTotalVolume(), 10.0);
        assertEquals(calculator.getVWAP(), 18000.0);
    }
    @DisplayName("수량이 0 일 경우 계산되지 않는다.")
    @Test
    void testVWAPwith0VolumeTrade() {
        calculator.recordTrade(18000.0,0);
        assertEquals(calculator.getTotalPriceVolume(), 0);
        assertEquals(calculator.getTotalVolume(), 0);
        assertEquals(calculator.getVWAP(), 0);
    }
    @DisplayName("금액이 0원일 경우 계산되지 않는다.")
    @Test
    void testVWAPwith0PriceTrade() {
        String ticker = "BTC";
        PlatformVWAPState platformVwapState = new PlatformVWAPState(ticker);
        calculator.recordTrade(0,100);
        assertEquals(calculator.getTotalPriceVolume(), 0);
        assertEquals(calculator.getTotalVolume(), 100);
        assertEquals(calculator.getVWAP(), 0);
    }
    @DisplayName("trade 건별로 누적되어 vwap 계산한다.")
    @Test
    void testVWAPStackTrades() {
        calculator.recordTrade(18000, 100);
        calculator.recordTrade(17000, 70);
        calculator.recordTrade(16000, 50);
        calculator.recordTrade(15000, 30);
        calculator.recordTrade(14000, 10);
        assertEquals(calculator.getTotalPriceVolume(), 4380000.0);
        assertEquals(calculator.getTotalVolume(), 260.0);
        assertEquals(calculator.getVWAP(), 16846.1538, 0.0001);
    }
    @DisplayName("수량이 0일 경우 체결 건이 있어도 적용되지 않는다.")
    @Test
    void testVWAPStackTradesWith0Volumes() {
        calculator.recordTrade(18000, 0);
        calculator.recordTrade(17000, 0);
        calculator.recordTrade(16000, 0);
        calculator.recordTrade(15000, 0);
        calculator.recordTrade(14000, 0);
        assertEquals(calculator.getTotalPriceVolume(), 0);
        assertEquals(calculator.getTotalVolume(), 0);
        assertEquals(calculator.getVWAP(), 0.0);
    }
    @DisplayName("여러 체결 건 중 한 건만 수량이 있을 경우 그 건만 적용된다.")
    @Test
    void testVWAPStack1Trades(){
        calculator.recordTrade(18000,0);
        calculator.recordTrade(17000,0);
        calculator.recordTrade(16000,0);
        calculator.recordTrade(15000,0);
        calculator.recordTrade(14000,1);
        assertEquals(calculator.getTotalPriceVolume(), 14000.0);
        assertEquals(calculator.getTotalVolume(), 1);
        assertEquals(calculator.getVWAP(), 14000.0);
    }
    @DisplayName("쌓인 주문에서 일부를 제거한다.")
    @Test
    void testVWAPRemoveTrades() {
        calculator.recordTrade(18000, 100);
        calculator.recordTrade(17000, 70);
        calculator.recordTrade(16000, 50);
        calculator.recordTrade(15000, 30);
        calculator.recordTrade(14000, 10);
        calculator.removeTrade(18000, 100);
        calculator.removeTrade(17000, 70);
        calculator.removeTrade(16000, 50);
        calculator.removeTrade(15000, 30);
        assertEquals(calculator.getTotalPriceVolume(), 140000.0);
        assertEquals(calculator.getTotalVolume(), 10.0);
        assertEquals(calculator.getVWAP(), 14000.0);
    }

}