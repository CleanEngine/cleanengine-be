package com.cleanengine.coin.realitybot.domain;


import lombok.Getter;

@Getter
public class VWAPCalculator {
    private double totalPriceVolume;
    private double totalVolume;

    public void recordTrade(double price, double volume) {
        totalPriceVolume += price * volume;
        totalVolume += volume;
    }

    public void removeTrade(double price, double volume) {
        totalPriceVolume -= price * volume;
        totalVolume -= volume;
    }

    public double getVWAP() {
        return (totalVolume == 0) ? 0.0 : totalPriceVolume / totalVolume;
    }

}
