package com.cleanengine.coin.order.application.event;

import com.cleanengine.coin.order.application.AssetService;
import com.cleanengine.coin.trade.domain.event.TradeExecutedEvent;
import com.cleanengine.coin.trade.domain.model.Trade;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class SpringTradeExecutedUpdateAssetCurrentPriceHandler {

    private final AssetService assetService;

    @TransactionalEventListener
    public void onTradeExecutedEvent(TradeExecutedEvent tradeExecutedEvent) {
        Trade trade = tradeExecutedEvent.getTrade();
        assetService.updateCurrentPrice(trade.getTicker(), trade.getPrice());
    }

}
