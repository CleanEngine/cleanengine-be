package com.cleanengine.coin.order.adapter.in.event;

import com.cleanengine.coin.order.application.event.OrderCreated;
import com.cleanengine.coin.order.domain.spi.ActiveOrders;
import com.cleanengine.coin.order.domain.spi.ActiveOrdersManager;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class SpringOrderCreatedAddActiveOrderHandler {
    private final ActiveOrdersManager activeOrdersManager;

    @Order(1)
    @TransactionalEventListener
    public void handleOrderCreated(OrderCreated event) {
        ActiveOrders activeOrders = activeOrdersManager.getActiveOrders(event.order().getTicker());
        activeOrders.saveOrder(event.order());
    }
}
