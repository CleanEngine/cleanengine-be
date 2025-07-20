package com.cleanengine.coin.trade.adapter.out;

import com.cleanengine.coin.trade.application.TradeExecutedEvent;
import com.cleanengine.coin.trade.port.out.TradeExecutedEventPublisherPort;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class SpringTradeExecutedEventPublisher implements TradeExecutedEventPublisherPort {

    private final ApplicationEventPublisher publisher;

    @Override
    public void publish(TradeExecutedEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null");
        } else if (event.getTrade() == null) {
            throw new IllegalArgumentException("trade must not be null");
        } else if (event.getBuyOrderId() == null) {
            throw new IllegalArgumentException("buyOrderId must not be null");
        } else if (event.getSellOrderId() == null) {
            throw new IllegalArgumentException("sellOrderId must not be null");
        }
        publisher.publishEvent(event);
    }

}
