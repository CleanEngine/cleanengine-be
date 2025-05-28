package com.cleanengine.coin.order.adapter.in.event;

import com.cleanengine.coin.order.application.event.OrderCreated;
import com.cleanengine.coin.orderbook.application.service.UpdateOrderBookUsecase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class SpringOrderCreatedUpdateOrderBookHandler {
    private final UpdateOrderBookUsecase updateOrderBookUsecase;

    @TransactionalEventListener(OrderCreated.class)
    public void handleOrderCreated(OrderCreated event) {
        updateOrderBookUsecase.updateOrderBookOnNewOrder(event.order());
    }
}
