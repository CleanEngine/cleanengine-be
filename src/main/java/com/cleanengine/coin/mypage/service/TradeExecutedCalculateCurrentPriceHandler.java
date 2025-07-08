package com.cleanengine.coin.mypage.service;

import com.cleanengine.coin.mypage.infra.CurrentPriceCache;
import com.cleanengine.coin.trade.application.TradeExecutedEvent;
import com.cleanengine.coin.trade.entity.Trade;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class TradeExecutedCalculateCurrentPriceHandler {
    private final CurrentPriceCache currentPriceCache;

    @EventListener
    public void onTradeExecuted(TradeExecutedEvent event) {
        Trade trade = event.getTrade();
        System.out.println("갱신 완료됨 : "+trade.getTicker()+" / "+trade.getPrice());
        currentPriceCache.update(trade.getTicker(), trade.getPrice());
    }

}
