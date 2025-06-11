package com.cleanengine.coin.trade.event;

import com.cleanengine.coin.order.application.event.OrderInsertedToQueue;
import com.cleanengine.coin.trade.application.TradeFlowService;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@Order(4)
@RequiredArgsConstructor
public class OrderInsertedQueueStartMatchingHandler {
    private final Map<String, ExecutorService> tickerExecutorServices = new ConcurrentHashMap<>();
    private final TradeFlowService tradeFlowService;

    @EventListener
    public void handleOrderInserted(OrderInsertedToQueue orderInsertedToQueue) {
        String ticker = orderInsertedToQueue.order().getTicker();

        if(!tickerExecutorServices.containsKey(ticker)) {
            addThreadExecutor(ticker);
        }

        ExecutorService executorService = tickerExecutorServices.get(ticker);
        executorService.execute(() -> tradeFlowService.execMatchAndTrade(ticker));
    }

    protected synchronized void addThreadExecutor(String ticker) {
        if (tickerExecutorServices.containsKey(ticker)) {
            return;
        }

        ExecutorService executorService = Executors.newSingleThreadExecutor(r->{
            Thread thread = new Thread(r);
            thread.setName("Trade-" + ticker);
            return thread;
        });

        tickerExecutorServices.put(ticker, executorService);
    }

    @PreDestroy
    public void shutdown() {
        tickerExecutorServices.values().forEach(ExecutorService::shutdown);
    }
}
