package com.cleanengine.coin.trade.domain.exception;

import com.cleanengine.coin.order.domain.Order;
import lombok.Getter;

@Getter
public class TradeZeroOrderException extends RuntimeException {

    Order order;

    public TradeZeroOrderException(String message, Order order) {
        super(message);
        this.order = order;
    }

}
