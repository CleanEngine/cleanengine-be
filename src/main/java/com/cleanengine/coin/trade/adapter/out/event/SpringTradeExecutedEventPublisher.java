package com.cleanengine.coin.trade.adapter.out.event;

import com.cleanengine.coin.trade.application.port.out.TradeExecutedEventPublisherPort;
import com.cleanengine.coin.trade.domain.event.TradeExecutedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * 체결이 완료되었을 때 실시간 정보 전달(호가창, 차트, 체결내역 등)을 위해 발행
 */
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
