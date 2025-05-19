package com.cleanengine.coin.orderbook.dto;

import com.cleanengine.coin.orderbook.domain.OrderBookUnit;

public record OrderBookUnitInfo(
        Double price,
        Double size
){
    public OrderBookUnitInfo(OrderBookUnit orderBookUnit) {
        this(orderBookUnit.getPrice(), orderBookUnit.getSize());
    }
}
