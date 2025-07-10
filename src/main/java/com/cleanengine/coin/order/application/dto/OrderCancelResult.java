package com.cleanengine.coin.order.application.dto;

import com.cleanengine.coin.order.domain.BuyOrder;
import com.cleanengine.coin.order.domain.Order;
import com.cleanengine.coin.order.domain.OrderStatus;

public record OrderCancelResult(
    String ticker,
    Long orderId,
    OrderStatus orderStatus,
    String side,
    String orderType
) {
    public OrderCancelResult(Order order){
        this(order.getTicker(), order.getId(), order.getState(), getSide(order), getOrderType(order));
    }

    private static String getSide(Order order) {
        return order instanceof BuyOrder ? "bid" : "ask";
    }

    private static String getOrderType(Order order) {
        return order.getIsMarketOrder()? "market" : "limit";
    }
}
