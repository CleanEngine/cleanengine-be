package com.cleanengine.coin.orderbook.domain;

import lombok.Getter;
import lombok.Synchronized;

@Getter
public abstract class OrderBookUnit implements Comparable<OrderBookUnit> {
    protected final Double price;
    protected Double size;

    protected OrderBookUnit(Double price, Double size) {
        this.price = price;
        this.size = size;
    }

    @Synchronized
    public void addOrder(Double size) {
        this.size += size;
    }

    @Synchronized
    public void executeTrade(Double size) {
        this.size -= size;
    }
}
