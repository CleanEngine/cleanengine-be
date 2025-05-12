package com.cleanengine.coin.trade.application;

import com.cleanengine.coin.order.application.queue.OrderQueueManager;
import com.cleanengine.coin.order.domain.BuyOrder;
import com.cleanengine.coin.order.domain.SellOrder;
import com.cleanengine.coin.order.infra.BuyOrderRepository;
import com.cleanengine.coin.order.infra.SellOrderRepository;
import com.cleanengine.coin.trade.entity.Trade;
import com.cleanengine.coin.trade.infra.TradeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@DisplayName("체결 처리 테스트")
public class TradeQueueManagerTest {

    @Autowired
    BuyOrderRepository buyOrderRepository;
    @Autowired
    SellOrderRepository sellOrderRepository;
    @Autowired
    TradeRepository tradeRepository;
    @Autowired
    TradeBatchProcessor tradeBatchProcessor;

    @DisplayName("지정가-지정가 완전체결 테스트")
    @Test
    public void testSimpleTrade() {
        String ticker = "BTC";

        BuyOrder buyOrder = BuyOrder.createLimitBuyOrder(ticker, 1, 10.0, 130000000.0, LocalDateTime.now(), false);
        SellOrder sellOrder = SellOrder.createLimitSellOrder(ticker, 2, 10.0, 130000000.0, LocalDateTime.now(), false);

        buyOrderRepository.save(buyOrder);
        sellOrderRepository.save(sellOrder);

        List<TradeQueueManager> tradeQueueManagers = tradeBatchProcessor.getTradeQueueManagers();
        OrderQueueManager orderQueueManager = tradeQueueManagers.get(0).getOrderQueueManager();
        orderQueueManager.addOrder(buyOrder);
        orderQueueManager.addOrder(sellOrder);

        try {
            // 체결이 완료될 때까지 대기 (최대 3초)
            await()
                    .atMost(3, TimeUnit.SECONDS)
                    .pollInterval(100, TimeUnit.MILLISECONDS)
                    .until(() -> {
                        List<Trade> trades = tradeRepository.findByBuyUserIdAndTicker(1, ticker);
                        return !trades.isEmpty();
                    });

            List<Trade> byBuyUserIdAndTicker = tradeRepository.findByBuyUserIdAndTicker(1, ticker);
            List<Trade> bySellUserIdAndTicker = tradeRepository.findBySellUserIdAndTicker(2, ticker);
            assertNotNull(byBuyUserIdAndTicker, "구매자의 거래 내역이 null이면 안됩니다");
            assertEquals(1, byBuyUserIdAndTicker.size(), "구매자의 거래 내역이 정확히 1개여야 합니다");

            assertNotNull(bySellUserIdAndTicker, "판매자의 거래 내역이 null이면 안됩니다");
            assertEquals(1, bySellUserIdAndTicker.size(), "판매자의 거래 내역이 정확히 1개여야 합니다");

            assertEquals(
                    byBuyUserIdAndTicker.getFirst().getTradeId(),
                    bySellUserIdAndTicker.getFirst().getTradeId(),
                    "구매자와 판매자의 거래 내역은 동일한 거래를 가리켜야 합니다"
            );
        } finally {
            // 테스트 종료 시 무한루프 중지
            tradeBatchProcessor.shutdown();
        }
    }

    @AfterEach
    void tearDown() {
        // 각 테스트 종료 후 무한루프 중지
        tradeBatchProcessor.shutdown();
        // 모든 스레드가 정리될 때까지 잠시 대기
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}