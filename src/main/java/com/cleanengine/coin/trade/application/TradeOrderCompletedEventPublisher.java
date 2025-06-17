package com.cleanengine.coin.trade.application;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class TradeOrderCompletedEventPublisher {

    private final ApplicationEventPublisher publisher;

    public TradeOrderCompletedEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    public void publish(TradeOrderCompletedEvent event) {
        publisher.publishEvent(event);
    }

}
