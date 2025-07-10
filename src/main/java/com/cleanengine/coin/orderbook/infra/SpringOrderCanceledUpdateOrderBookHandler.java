package com.cleanengine.coin.orderbook.infra;

import com.cleanengine.coin.orderbook.application.service.UpdateOrderBookUsecase;
import com.cleanengine.coin.orderbook.dto.OrderCanceled;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class SpringOrderCanceledUpdateOrderBookHandler {
    private final UpdateOrderBookUsecase updateOrderBookUsecase;

    @TransactionalEventListener
    public void handleOrderCanceled(OrderCanceled event) {
        updateOrderBookUsecase.updateOrderBookOnOrderCanceled(event.order());
    }
}
