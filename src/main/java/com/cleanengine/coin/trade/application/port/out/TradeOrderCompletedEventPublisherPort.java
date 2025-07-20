package com.cleanengine.coin.trade.application.port.out;

import com.cleanengine.coin.trade.domain.event.TradeOrderCompletedEvent;

public interface TradeOrderCompletedEventPublisherPort {

    void publish(TradeOrderCompletedEvent event);

}
