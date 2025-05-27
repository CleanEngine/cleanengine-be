package com.cleanengine.coin.realitybot.vo;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.NavigableMap;
import java.util.TreeMap;

@Service
public class UnitPricePolicy {
    private final NavigableMap<Double, Double> unitPriceRules = new TreeMap<Double, Double>();

    @PostConstruct
    public void initRules() {
        unitPriceRules.put(0.0001,0.00000001);
        unitPriceRules.put(0.001,0.0000001);
        unitPriceRules.put(0.01,0.000001);
        unitPriceRules.put(0.1,0.00001);
        unitPriceRules.put(1.0,0.0001);
        unitPriceRules.put(10.0,0.001);
        unitPriceRules.put(100.0,0.01);
        unitPriceRules.put(1_000.0,0.1);
        unitPriceRules.put(10_000.0,1.0);
        unitPriceRules.put(100_000.0,10.0);
        unitPriceRules.put(500_000.0,50.0);
        unitPriceRules.put(1_000_000.0,100.0);
        unitPriceRules.put(5_000_000.0,500.0);
        unitPriceRules.put(10_000_000.0,1_000.0);
    }

    public double getUnitPrice(double apiTradePrice){
        double unitprice =unitPriceRules.higherEntry(apiTradePrice).getValue();
        System.out.println("========================================"+unitprice);
        return unitprice;
    }
}
