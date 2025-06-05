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

    private TradeExecutedEvent(Trade trade, Long buyOrderId, Long sellOrderId) {
        this.trade = trade;
        this.buyOrderId = buyOrderId;
        this.sellOrderId = sellOrderId;
    }

    public static TradeExecutedEvent of(Trade trade, Long buyOrderId, Long sellOrderId) {
        return new TradeExecutedEvent(trade, buyOrderId, sellOrderId);
    }

}
