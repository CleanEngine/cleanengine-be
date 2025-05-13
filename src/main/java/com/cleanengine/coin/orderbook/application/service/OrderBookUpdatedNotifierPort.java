package com.cleanengine.coin.orderbook.application.service;

import com.cleanengine.coin.orderbook.dto.OrderBookInfo;

public interface OrderBookUpdatedNotifierPort {
    void sendOrderBooks(OrderBookInfo orderBookInfo);
}
