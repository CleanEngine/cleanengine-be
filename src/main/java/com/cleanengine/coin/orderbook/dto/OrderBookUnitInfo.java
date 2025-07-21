package com.cleanengine.coin.orderbook.dto;

import com.cleanengine.coin.orderbook.domain.OrderBookUnit;

public record OrderBookUnitInfo(
        Double price,
        Double size,
        Double priceChangePercent
){
    public OrderBookUnitInfo{
        if(price == null || size == null || priceChangePercent == null){
            throw new IllegalArgumentException("price, size, priceChangePercent cannot be null.");
        }
    }

    public OrderBookUnitInfo(OrderBookUnit orderBookUnit, Double yesterdayClosingPrice) {
        this(orderBookUnit.getPrice(),
                orderBookUnit.getSize(),
                calculateChangePercent(orderBookUnit.getPrice(), yesterdayClosingPrice));
    }

    private static Double calculateChangePercent(Double price, Double yesterdayClosingPrice) {
        if(price == null || yesterdayClosingPrice == null){
            throw new IllegalArgumentException("price, yesterdayClosingPrice cannot be null.");
        }

        return (yesterdayClosingPrice <= 0) ?
                0.0 : (price - yesterdayClosingPrice) / yesterdayClosingPrice * 100;
    }
}
