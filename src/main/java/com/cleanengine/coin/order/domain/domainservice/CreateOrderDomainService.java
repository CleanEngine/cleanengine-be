package com.cleanengine.coin.order.domain.domainservice;

import com.cleanengine.coin.order.domain.Order;

import java.time.LocalDateTime;

public interface CreateOrderDomainService<T extends Order> {
    T createOrder(String ticker, Integer userId, Boolean isBuyOrder, Boolean isMarketOrder,
                  Double orderSize, Double price, LocalDateTime createdAt, Boolean isBot);
}

