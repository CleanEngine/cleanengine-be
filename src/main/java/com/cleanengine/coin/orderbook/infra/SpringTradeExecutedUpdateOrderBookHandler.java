package com.cleanengine.coin.orderbook.infra;

import com.cleanengine.coin.orderbook.application.service.UpdateOrderBookUsecase;
import com.cleanengine.coin.trade.application.TradeExecutedEvent;
import com.cleanengine.coin.trade.entity.Trade;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class SpringTradeExecutedUpdateOrderBookHandler {
    private final UpdateOrderBookUsecase updateOrderBookUsecase;

    @TransactionalEventListener
    public void onTradeExecutedEvent(TradeExecutedEvent tradeExecutedEvent) {
        Trade trade = tradeExecutedEvent.getTrade();
        updateOrderBookUsecase.updateOrderBookOnTradeExecuted(trade.getTicker(),
                tradeExecutedEvent.getBuyOrderId(), tradeExecutedEvent.getSellOrderId(), trade.getSize());
    }
}
