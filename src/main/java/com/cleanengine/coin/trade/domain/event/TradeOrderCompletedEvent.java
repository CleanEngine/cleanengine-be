package com.cleanengine.coin.trade.domain.event;

import com.cleanengine.coin.order.domain.Order;
import lombok.Builder;
import lombok.Getter;

/**
 * 체결 후 사용자의 주문이 완전 체결되었을 때 사용자에게 알림을 주기 위한 이벤트
 */
@Getter
@Builder
public class TradeOrderCompletedEvent {

    Order order;

    private TradeOrderCompletedEvent(Order order) {
        this.order = order;
    }

    public static TradeOrderCompletedEvent of(Order order) {
        return new TradeOrderCompletedEvent(order);
    }

}
