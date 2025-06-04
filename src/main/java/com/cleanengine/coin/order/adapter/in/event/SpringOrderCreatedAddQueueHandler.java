package com.cleanengine.coin.order.adapter.in.event;

import com.cleanengine.coin.order.application.event.OrderCreated;
import com.cleanengine.coin.order.application.event.OrderInsertedToQueue;
import com.cleanengine.coin.order.domain.Order;
import com.cleanengine.coin.order.domain.spi.WaitingOrders;
import com.cleanengine.coin.order.domain.spi.WaitingOrdersManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class SpringOrderCreatedAddQueueHandler {
    private final WaitingOrdersManager waitingOrdersManager;
    private final ApplicationEventPublisher applicationEventPublisher;

    @TransactionalEventListener(OrderCreated.class)
    public void handleOrderCreated(OrderCreated event) {
        Order order = event.order();
        WaitingOrders waitingOrders = waitingOrdersManager.getWaitingOrders(order.getTicker());
        waitingOrders.addOrder(order);
        applicationEventPublisher.publishEvent(new OrderInsertedToQueue(order));
    }
}
