package com.cleanengine.coin.trade.application;

import com.cleanengine.coin.order.application.event.OrderInsertedToQueue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TradeQueueManager {

    private final TradeFlowService tradeFlowService;

    public TradeQueueManager(TradeFlowService tradeFlowService) {
        this.tradeFlowService = tradeFlowService;
    }

    @EventListener
    public void handleOrderInserted(OrderInsertedToQueue orderInsertedToQueue) {
        String ticker = orderInsertedToQueue.order().getTicker();
        tradeFlowService.execMatchAndTrade(ticker);
    }

}