package com.cleanengine.coin.orderbook.application.service;

import com.cleanengine.coin.orderbook.domain.OrderPriceInfo;

import java.util.List;

public interface OrderBookUpdatedPort {
    void sendOrderBooks(List<OrderPriceInfo> buyOrderBookList, List<OrderPriceInfo> sellOrderBookList);
}
