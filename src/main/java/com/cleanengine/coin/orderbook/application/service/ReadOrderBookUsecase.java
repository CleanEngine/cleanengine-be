package com.cleanengine.coin.orderbook.application.service;

import com.cleanengine.coin.orderbook.dto.OrderBookInfo;

public interface ReadOrderBookUsecase {
    OrderBookInfo getOrderBook(String ticker);
}
