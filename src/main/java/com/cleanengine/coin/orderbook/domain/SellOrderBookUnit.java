package com.cleanengine.coin.orderbook.domain;

public class SellOrderBookUnit extends OrderBookUnit {
    public SellOrderBookUnit(Double price, Double size) {
        super(price, size);
    }

    // ConcurrnetSkipListSet에서 음수인애가 first가 된다. first를 우선순위 높게 생각해야 한다.
    // sellOrder에서는 매도 가격이 낮은 것이 우선순위 높은 것이므로, 가격이 낮은애가 음수가 되어야 한다.
    @Override
    public int compareTo(OrderBookUnit o) {
        return Double.compare(this.price, o.price);
    }
}
