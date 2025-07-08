package com.cleanengine.coin.order.adapter.out.event;

import com.cleanengine.coin.order.application.port.out.PublishOrderCanceledPort;
import com.cleanengine.coin.orderbook.dto.OrderCanceled;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class SpringOrderCanceledPublisher implements PublishOrderCanceledPort {
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publish(OrderCanceled orderCanceled) {
        applicationEventPublisher.publishEvent(orderCanceled);
    }
}
