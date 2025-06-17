package com.cleanengine.coin.trade.application;

import com.cleanengine.coin.order.domain.Order;
import com.cleanengine.coin.trade.entity.Trade;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TradeOrderCompletedEventImpl implements TradeOrderCompletedEvent {

    Order order;

    private TradeOrderCompletedEventImpl(Order order) {
        this.order = order;
    }

    public static TradeOrderCompletedEventImpl of(Order order) {
        return new TradeOrderCompletedEventImpl(order);
    }

}
