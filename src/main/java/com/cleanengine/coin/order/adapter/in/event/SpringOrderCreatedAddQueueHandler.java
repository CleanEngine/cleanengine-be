package com.cleanengine.coin.order.adapter.in.event;

import com.cleanengine.coin.order.application.event.OrderCreated;
import com.cleanengine.coin.order.adapter.out.persistentce.order.queue.OrderQueueManagerPool;
import com.cleanengine.coin.order.domain.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class SpringOrderCreatedAddQueueHandler {
    private final OrderQueueManagerPool orderQueueManagerPool;

    @TransactionalEventListener(OrderCreated.class)
    public void handleOrderCreated(OrderCreated event) {
        Order order = event.order();
        orderQueueManagerPool.addOrder(order.getTicker(), order);
    }
}
