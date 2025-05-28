package com.cleanengine.coin.order.domain.spi;

import com.cleanengine.coin.common.domain.port.PriorityQueueStore;
import com.cleanengine.coin.order.domain.BuyOrder;
import com.cleanengine.coin.order.domain.Order;
import com.cleanengine.coin.order.domain.OrderType;
import com.cleanengine.coin.order.domain.SellOrder;

import java.io.Closeable;

public interface WaitingOrders extends Closeable {

    String getTicker();

    void addOrder(Order order);

    PriorityQueueStore<BuyOrder> getBuyOrderPriorityQueueStore(OrderType orderType);
    PriorityQueueStore<SellOrder> getSellOrderPriorityQueueStore(OrderType orderType);

    // TODO removeOrder 여기에 있는 것이 나을지 고민
    void removeOrder(Order order);

    void clearAllQueues();

    @Override
    void close();
}
