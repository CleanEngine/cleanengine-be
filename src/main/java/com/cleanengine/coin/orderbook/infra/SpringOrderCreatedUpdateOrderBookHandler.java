package com.cleanengine.coin.orderbook.infra;

import com.cleanengine.coin.order.application.event.OrderCreated;
import com.cleanengine.coin.orderbook.application.service.UpdateOrderBookUsecase;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class SpringOrderCreatedUpdateOrderBookHandler {
    private final UpdateOrderBookUsecase updateOrderBookUsecase;

    @Order(1)
    @TransactionalEventListener
    public void handleOrderCreated(OrderCreated event) {
        updateOrderBookUsecase.updateOrderBookOnNewOrder(event.order());
    }
}
