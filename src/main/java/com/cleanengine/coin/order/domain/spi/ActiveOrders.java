package com.cleanengine.coin.order.domain.spi;

import com.cleanengine.coin.common.domain.port.KeyValueStore;
import com.cleanengine.coin.order.domain.Order;

import java.util.Optional;

public interface ActiveOrders {
    String getTicker();

    void saveOrder(Order order);

    Optional<Order> getOrder(Long orderId);

    Optional<Order> removeOrder(Long orderId);

    KeyValueStore<Long, Order> getOrderKeyValueStore();
}
