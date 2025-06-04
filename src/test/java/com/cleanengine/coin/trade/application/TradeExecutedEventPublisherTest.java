package com.cleanengine.coin.trade.application;

import com.cleanengine.coin.trade.entity.Trade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class TradeExecutedEventPublisherTest {

    @DisplayName("체결 내역 이벤트를 발행한다.")
    @Test
    void simplePublish() {
        // given
        ApplicationEventPublisher mockPublisher = Mockito.mock(ApplicationEventPublisher.class);
        TradeExecutedEventPublisher publisher = new TradeExecutedEventPublisher(mockPublisher);
        Trade newTrade = Trade.of("BTC", LocalDateTime.now(), 2, 1, 1000.0, 10.0);
        TradeExecutedEvent tradeExecutedEvent = TradeExecutedEvent.of(newTrade, 1L, 2L);

        // when
        publisher.publish(tradeExecutedEvent);

        // then
        verify(mockPublisher, times(1))
                .publishEvent(tradeExecutedEvent);

    }

}