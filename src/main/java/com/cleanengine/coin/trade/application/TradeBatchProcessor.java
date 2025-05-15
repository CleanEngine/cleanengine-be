package com.cleanengine.coin.trade.application;

import com.cleanengine.coin.chart.dto.TradeEventDto;
import com.cleanengine.coin.order.application.queue.OrderQueueManagerPool;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
public class TradeBatchProcessor implements ApplicationRunner {

    Logger logger = LoggerFactory.getLogger(TradeBatchProcessor.class);

    private final OrderQueueManagerPool orderQueueManagerPool;
    private final TradeService tradeService;
    private final List<ExecutorService> executors = new ArrayList<>();

    @Getter
    private final Map<String, TradeQueueManager> tradeQueueManagers = new HashMap<>();

    @Value("${order.tickers}") String[] tickers;

    public TradeBatchProcessor(OrderQueueManagerPool orderQueueManagerPool, TradeService tradeService) {
        this.orderQueueManagerPool = orderQueueManagerPool;
        this.tradeService = tradeService;
    }

    @Override
    public void run(ApplicationArguments args) {
        processTrades();
    }

    private void processTrades() {
        for (String ticker : tickers) {
            TradeQueueManager tradeQueueManager = new TradeQueueManager(orderQueueManagerPool.getOrderQueueManager(ticker),
                    tradeService);
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
                    logger.error("Error in trade loop for {}: {}",ticker, e.getMessage());
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
                        System.err.println("스레드풀이 완전히 종료되지 않았습니다");
                    }
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    public TradeEventDto retrieveTradeEventDto(String ticker) {
        TradeQueueManager tradeQueueManager = this.tradeQueueManagers.get(ticker);
        TradeEventDto lastTradeEventDto = tradeQueueManager.getLastTradeEventDto();

        // 서비스 시작 후 체결 내역이 없으면 null 반환
        if (lastTradeEventDto.getSize() == 0.0 || lastTradeEventDto.getPrice() == 0.0) {
            return null;
        }
        return lastTradeEventDto;
    }

}