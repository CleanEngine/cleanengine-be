package com.cleanengine.coin.order.application.port.out;

import com.cleanengine.coin.order.application.event.OrderCreated;

public interface PublishOrderCreatedPort {
    void publish(OrderCreated orderCreated);
}
