package com.cleanengine.coin.orderbook.application.service;

import com.cleanengine.coin.order.domain.Order;

// TODO 메서드 parameter가 비슷한 스타일이어야 한다.
public interface UpdateOrderBookUsecase {
    void updateOrderBookOnNewOrder(Order order);
    void updateOrderBookOnTradeExecuted(String ticker, Long buyOrderId, Long sellOrderId, Double orderSize);
}
