package com.cleanengine.coin.trade.application;

import com.cleanengine.coin.order.domain.Order;

public interface TradeOrderCompletedEvent {

    Order getOrder();

}
