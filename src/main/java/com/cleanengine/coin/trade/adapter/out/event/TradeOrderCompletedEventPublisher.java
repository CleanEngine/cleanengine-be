package com.cleanengine.coin.trade.adapter.out.event;

import com.cleanengine.coin.trade.application.port.out.TradeOrderCompletedEventPublisherPort;
import com.cleanengine.coin.trade.domain.event.TradeOrderCompletedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * 체결 후 사용자의 주문이 완전 체결되었을 때 사용자에게 알리기 위해 이벤트 발행
 */
@Service
public class TradeOrderCompletedEventPublisher implements TradeOrderCompletedEventPublisherPort {

    private final ApplicationEventPublisher publisher;

    public TradeOrderCompletedEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    public void publish(TradeOrderCompletedEvent event) {
        publisher.publishEvent(event);
    }

}
