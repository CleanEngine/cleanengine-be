package com.cleanengine.coin.trade.port.out;

import com.cleanengine.coin.trade.application.TradeExecutedEvent;

public interface TradeExecutedEventPublisherPort {

    void publish(TradeExecutedEvent event);

}
