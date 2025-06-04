package com.cleanengine.coin.trade.application;

import com.cleanengine.coin.trade.entity.Trade;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TradeExecutedEvent {
    Trade trade;
    Long buyOrderId;
    Long sellOrderId;
}
