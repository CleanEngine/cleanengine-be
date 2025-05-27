package com.cleanengine.coin.order.infra.event;

import com.cleanengine.coin.order.application.event.OrderCreated;
import com.cleanengine.coin.order.application.port.out.PublishOrderCreatedPort;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpringPublishOrderCreatedPort implements PublishOrderCreatedPort {
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publish(OrderCreated orderCreated) {
        applicationEventPublisher.publishEvent(orderCreated);
    }
}
