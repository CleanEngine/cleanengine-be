package com.cleanengine.coin.order.domain.spi;

import com.cleanengine.coin.common.domain.port.KeyValueStore;
import com.cleanengine.coin.order.domain.BuyOrder;
import com.cleanengine.coin.order.domain.Order;
import com.cleanengine.coin.order.domain.SellOrder;

import java.util.Optional;

public interface ActiveOrders {
    String getTicker();

    void saveOrder(Order order);

    Optional<Order> getOrder(Long orderId, boolean isBuyOrder);

    Optional<Order> removeOrder(Long orderId, boolean isBuyOrder);

    KeyValueStore<Long, BuyOrder> getBuyOrderKeyValueStore();
    KeyValueStore<Long, SellOrder> getSellOrderKeyValueStore();
}
