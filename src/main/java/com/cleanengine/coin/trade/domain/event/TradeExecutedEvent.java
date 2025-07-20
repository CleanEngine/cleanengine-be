package com.cleanengine.coin.trade.domain.event;

import com.cleanengine.coin.trade.domain.model.Trade;
import lombok.Builder;
import lombok.Getter;

/**
 * 체결이 완료되었을 때 실시간 정보 전달(호가창, 차트, 체결내역 등)을 위해 발행하는 이벤트
 */
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
