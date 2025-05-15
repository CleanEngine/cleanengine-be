package com.cleanengine.coin.order.infra;

import com.cleanengine.coin.order.application.OrderCreated;
import com.cleanengine.coin.order.application.queue.OrderQueueManagerPool;
import com.cleanengine.coin.order.domain.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class OrderCreatedEventHandler {
    private final OrderQueueManagerPool orderQueueManagerPool;

    @TransactionalEventListener(OrderCreated.class)
    public void handleOrderCreated(OrderCreated event) {
        Order order = event.getOrder();
        orderQueueManagerPool.addOrder(order.getTicker(), order);
    }
}
