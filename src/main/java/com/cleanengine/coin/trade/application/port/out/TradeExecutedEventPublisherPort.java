package com.cleanengine.coin.trade.application.port.out;

import com.cleanengine.coin.trade.domain.event.TradeExecutedEvent;

public interface TradeExecutedEventPublisherPort {

    void publish(TradeExecutedEvent event);

}
