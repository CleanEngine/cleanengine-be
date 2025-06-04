package com.cleanengine.coin.trade.application;

import com.cleanengine.coin.order.domain.spi.WaitingOrdersManager;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Order(4)
@Slf4j
@RequiredArgsConstructor
@Service
public class TradeBatchProcessor implements ApplicationRunner {

    private final WaitingOrdersManager waitingOrdersManager;
    private final TradeFlowService tradeFlowService;
    private final List<ExecutorService> executors = new ArrayList<>();

    @Getter
    private final Map<String, TradeQueueManager> tradeQueueManagers = new HashMap<>();

    @Value("${order.tickers}") String[] tickers;

    @Override
    public void run(ApplicationArguments args) {
        processTrades();
    }

    private void processTrades() {
        for (String ticker : tickers) {
            TradeQueueManager tradeQueueManager = new TradeQueueManager(waitingOrdersManager.getWaitingOrders(ticker),
                    tradeFlowService);
            tradeQueueManagers.put(ticker, tradeQueueManager); // 정상 종료를 위해 저장

            ExecutorService tradeExecutor = Executors.newSingleThreadExecutor(r -> {
                Thread thread = new Thread(r);
                thread.setName("Trade-" + ticker);
                return thread;
            });
            executors.add(tradeExecutor);

            tradeExecutor.submit(() -> {
                try {
                    tradeQueueManager.run();
                } catch (Exception e) {
                    log.error("Error in trade loop for {}: {}",ticker, e.getMessage());
                }
            });
        }
    }

    @PreDestroy
    public void shutdown() {
        // 무한루프 종료
        tradeQueueManagers.forEach((ticker, manager) -> manager.stop());

        // 스레드풀 종료
        for (ExecutorService executor : executors) {
            try {
                executor.shutdown();

                // 2초 동안 종료 대기 후 강제 종료
                if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                    // 추가로 1초 더 대기
                    if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
                        log.error("스레드풀이 완전히 종료되지 않았습니다");
                    }
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

}