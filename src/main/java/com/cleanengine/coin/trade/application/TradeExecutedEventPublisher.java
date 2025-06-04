package com.cleanengine.coin.trade.application;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class TradeExecutedEventPublisher {

    private final ApplicationEventPublisher publisher;

    public TradeExecutedEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    public void publish(TradeExecutedEvent event) {
        publisher.publishEvent(event);
    }

}
