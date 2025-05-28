package com.cleanengine.coin.trade.application;

import com.cleanengine.coin.order.domain.spi.WaitingOrders;
import com.cleanengine.coin.orderbook.application.service.UpdateOrderBookUsecase;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TradeQueueManager {

    private volatile boolean running = true; // 무한루프 종료 플래그

    private final String ticker;
    private final TradeService tradeService;

    public TradeQueueManager(WaitingOrders waitingOrders, UpdateOrderBookUsecase updateOrderBookUsecase, TradeService tradeService) {
        this.tradeService = tradeService;
        this.ticker = waitingOrders.getTicker();
    }

    public void run() {
        // TODO : 주문 시 이벤트 기반으로 동작하도록 개선
        while (running) {
            try {
                tradeService.execMatchAndTrade(ticker);
            } catch (Exception e) {
                log.error("Error processing trades for {}: {}", this.ticker, e.getMessage());
            }
        }
    }

    public void stop() {
        this.running = false; // 무한루프 종료 플래그
    }

}