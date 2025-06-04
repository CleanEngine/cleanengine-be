package com.cleanengine.coin.trade.application;

import com.cleanengine.coin.order.application.event.OrderCreated;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
public class TradeQueueManager {

    private final TradeFlowService tradeFlowService;

    public TradeQueueManager(TradeFlowService tradeFlowService) {
        this.tradeFlowService = tradeFlowService;
    }

    @TransactionalEventListener
    public void handleOrderInserted(OrderCreated event) {
        try {
            tradeFlowService.execMatchAndTrade(event.order().getTicker());
        } catch (Exception e) {
            log.error("Error processing trades for {}: {}", event.order().getTicker(), e.getMessage());
        }
    }

}