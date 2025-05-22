package com.cleanengine.coin.trade.application;

import com.cleanengine.coin.order.domain.BuyOrder;
import com.cleanengine.coin.order.domain.Order;
import com.cleanengine.coin.order.domain.SellOrder;

public record TradePair<T extends Order, U extends Order>(T first, U second) {
    public TradePair {
        if ((first instanceof BuyOrder && second instanceof BuyOrder) ||
                (first instanceof SellOrder && second instanceof SellOrder)) {
            throw new IllegalArgumentException("매수 주문과 매도 주문이 각각 하나씩 매칭되어야 합니다.");
        }
    }

    public BuyOrder getBuyOrder() {
        return (first instanceof BuyOrder) ? (BuyOrder) first : (BuyOrder) second;
    }

    public SellOrder getSellOrder() {
        return (first instanceof SellOrder) ? (SellOrder) first : (SellOrder) second;
    }

    public static <T extends Order, U extends Order> TradePair<T, U> of(T order1, U order2) {
        return new TradePair<>(order1, order2);
    }
}
