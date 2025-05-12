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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DisplayName("체결 처리 테스트")
public class TradeQueueManagerTest {

    private static final Logger logger = LoggerFactory.getLogger(TradeQueueManagerTest.class);

    @Autowired
    BuyOrderRepository buyOrderRepository;
    @Autowired
    SellOrderRepository sellOrderRepository;
    @Autowired
    TradeRepository tradeRepository;
    @Autowired
    TradeBatchProcessor tradeBatchProcessor;

    // TODO : 각 객체의 값까지 정합성이 맞는지 테스트 필요

    @DisplayName("지정가매수-지정가매도 완전체결")
    @Test
    public void testLimitToLimitCompleteTrade() {
        String ticker = "BTC";

        BuyOrder buyOrder = BuyOrder.createLimitBuyOrder(ticker, 1, 10.0, 130_000_000.0, LocalDateTime.now(), false);
        SellOrder sellOrder = SellOrder.createLimitSellOrder(ticker, 2, 10.0, 130_000_000.0, LocalDateTime.now(), false);

        buyOrderRepository.save(buyOrder);
        sellOrderRepository.save(sellOrder);

        List<TradeQueueManager> tradeQueueManagers = tradeBatchProcessor.getTradeQueueManagers();
        OrderQueueManager orderQueueManager = tradeQueueManagers.getFirst().getOrderQueueManager();
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
            assertNotNull(byBuyUserIdAndTicker, "매수인의 거래 내역이 null이면 안됩니다");
            assertEquals(1, byBuyUserIdAndTicker.size(), "매수인의 거래 내역이 정확히 1개여야 합니다");

            assertNotNull(bySellUserIdAndTicker, "매도인의 거래 내역이 null이면 안됩니다");
            assertEquals(1, bySellUserIdAndTicker.size(), "매도인의 거래 내역이 정확히 1개여야 합니다");

            assertEquals(
                    byBuyUserIdAndTicker.getFirst().getTradeId(),
                    bySellUserIdAndTicker.getFirst().getTradeId(),
                    "매수인와 매도인의 거래 내역은 동일한 거래를 가리켜야 합니다"
            );

            assertTrue(orderQueueManager.getLimitBuyOrderQueue().isEmpty(), "남은 지정가 매수 주문이 없어야 합니다.");
            assertTrue(orderQueueManager.getLimitSellOrderQueue().isEmpty(), "남은 지정가 매도 주문이 없어야 합니다.");
        } finally {
            // 테스트 종료 시 무한루프 중지
            tradeBatchProcessor.shutdown();
        }
    }

    @DisplayName("지정가매수-지정가매도 매도부분체결")
    @Test
    public void testLimitToLimitPartialTrade1() {
        String ticker = "BTC";

        BuyOrder buyOrder = BuyOrder.createLimitBuyOrder(ticker, 1, 5.0, 130_000_000.0, LocalDateTime.now(), false);
        SellOrder sellOrder = SellOrder.createLimitSellOrder(ticker, 2, 10.0, 130_000_000.0, LocalDateTime.now(), false);

        buyOrderRepository.save(buyOrder);
        sellOrderRepository.save(sellOrder);

        List<TradeQueueManager> tradeQueueManagers = tradeBatchProcessor.getTradeQueueManagers();
        OrderQueueManager orderQueueManager = tradeQueueManagers.getFirst().getOrderQueueManager();
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
            assertNotNull(byBuyUserIdAndTicker, "매수인의 거래 내역이 null이면 안됩니다");
            assertEquals(1, byBuyUserIdAndTicker.size(), "매수인의 거래 내역이 정확히 1개여야 합니다");

            assertNotNull(bySellUserIdAndTicker, "매도인의 거래 내역이 null이면 안됩니다");
            assertEquals(1, bySellUserIdAndTicker.size(), "매도인의 거래 내역이 정확히 1개여야 합니다");

            assertEquals(byBuyUserIdAndTicker.getFirst().getTradeId(),
                    bySellUserIdAndTicker.getFirst().getTradeId(),
                    "매수인와 매도인의 거래 내역은 동일한 거래를 가리켜야 합니다"
            );

            assertTrue(orderQueueManager.getLimitBuyOrderQueue().isEmpty(), "남은 지정가 매수 주문이 없어야 합니다.");
            assertEquals(1, orderQueueManager.getLimitSellOrderQueue().size(), "지정가 매도 주문이 1개 남아있어야 합니다.");
        } finally {
            // 테스트 종료 시 무한루프 중지
            tradeBatchProcessor.shutdown();
        }
    }

    @DisplayName("지정가매수-지정가매도 매수부분체결")
    @Test
    public void testLimitToLimitPartialTrade2() {
        String ticker = "BTC";

        BuyOrder buyOrder = BuyOrder.createLimitBuyOrder(ticker, 1, 10.0, 130_000_000.0, LocalDateTime.now(), false);
        SellOrder sellOrder = SellOrder.createLimitSellOrder(ticker, 2, 5.0, 130_000_000.0, LocalDateTime.now(), false);

        buyOrderRepository.save(buyOrder);
        sellOrderRepository.save(sellOrder);

        List<TradeQueueManager> tradeQueueManagers = tradeBatchProcessor.getTradeQueueManagers();
        OrderQueueManager orderQueueManager = tradeQueueManagers.getFirst().getOrderQueueManager();
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
            assertNotNull(byBuyUserIdAndTicker, "매수인의 거래 내역이 null이면 안됩니다");
            assertEquals(1, byBuyUserIdAndTicker.size(), "매수인의 거래 내역이 정확히 1개여야 합니다");

            assertNotNull(bySellUserIdAndTicker, "매도인의 거래 내역이 null이면 안됩니다");
            assertEquals(1, bySellUserIdAndTicker.size(), "매도인의 거래 내역이 정확히 1개여야 합니다");

            assertEquals(byBuyUserIdAndTicker.getFirst().getTradeId(),
                    bySellUserIdAndTicker.getFirst().getTradeId(),
                    "매수인와 매도인의 거래 내역은 동일한 거래를 가리켜야 합니다"
            );

            assertEquals(1, orderQueueManager.getLimitBuyOrderQueue().size(), "지정가 매수 주문이 1개 남아있어야 합니다.");
            assertTrue(orderQueueManager.getLimitSellOrderQueue().isEmpty(), "남은 지정가 매도 주문이 없어야 합니다.");
        } finally {
            // 테스트 종료 시 무한루프 중지
            tradeBatchProcessor.shutdown();
        }
    }

    @DisplayName("시장가매수-지정가매도 완전체결")
    @Test
    public void testMarketToLimitCompleteTrade1() {
        String ticker = "BTC";

        BuyOrder buyOrder = BuyOrder.createMarketBuyOrder(ticker, 1, 1_300_000_000.0, LocalDateTime.now(), false);
        SellOrder sellOrder = SellOrder.createLimitSellOrder(ticker, 2, 10.0, 130_000_000.0, LocalDateTime.now(), false);

        buyOrderRepository.save(buyOrder);
        sellOrderRepository.save(sellOrder);

        List<TradeQueueManager> tradeQueueManagers = tradeBatchProcessor.getTradeQueueManagers();
        OrderQueueManager orderQueueManager = tradeQueueManagers.getFirst().getOrderQueueManager();
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
            assertNotNull(byBuyUserIdAndTicker, "매수인의 거래 내역이 null이면 안됩니다");
            assertEquals(1, byBuyUserIdAndTicker.size(), "매수인의 거래 내역이 정확히 1개여야 합니다");

            assertNotNull(bySellUserIdAndTicker, "매도인의 거래 내역이 null이면 안됩니다");
            assertEquals(1, bySellUserIdAndTicker.size(), "매도인의 거래 내역이 정확히 1개여야 합니다");

            assertEquals(byBuyUserIdAndTicker.getFirst().getTradeId(),
                    bySellUserIdAndTicker.getFirst().getTradeId(),
                    "매수인와 매도인의 거래 내역은 동일한 거래를 가리켜야 합니다"
            );

            assertTrue(orderQueueManager.getMarketBuyOrderQueue().isEmpty(), "남은 시장가 매수 주문이 없어야 합니다.");
            assertTrue(orderQueueManager.getLimitSellOrderQueue().isEmpty(), "남은 지정가 매도 주문이 없어야 합니다.");
        } finally {
            // 테스트 종료 시 무한루프 중지
            tradeBatchProcessor.shutdown();
        }
    }

    @DisplayName("지정가매수-시장가매도 완전체결")
    @Test
    public void testMarketToLimitCompleteTrade2() {
        String ticker = "BTC";

        BuyOrder buyOrder = BuyOrder.createLimitBuyOrder(ticker, 1, 10.0, 130_000_000.0, LocalDateTime.now(), false);
        SellOrder sellOrder = SellOrder.createMarketSellOrder(ticker, 2, 10.0, LocalDateTime.now(), false);

        buyOrderRepository.save(buyOrder);
        sellOrderRepository.save(sellOrder);

        List<TradeQueueManager> tradeQueueManagers = tradeBatchProcessor.getTradeQueueManagers();
        OrderQueueManager orderQueueManager = tradeQueueManagers.getFirst().getOrderQueueManager();
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
            assertNotNull(byBuyUserIdAndTicker, "매수인의 거래 내역이 null이면 안됩니다");
            assertEquals(1, byBuyUserIdAndTicker.size(), "매수인의 거래 내역이 정확히 1개여야 합니다");

            assertNotNull(bySellUserIdAndTicker, "매도인의 거래 내역이 null이면 안됩니다");
            assertEquals(1, bySellUserIdAndTicker.size(), "매도인의 거래 내역이 정확히 1개여야 합니다");

            assertEquals(byBuyUserIdAndTicker.getFirst().getTradeId(),
                    bySellUserIdAndTicker.getFirst().getTradeId(),
                    "매수인와 매도인의 거래 내역은 동일한 거래를 가리켜야 합니다"
            );

            assertTrue(orderQueueManager.getLimitBuyOrderQueue().isEmpty(), "남은 지정가 매수 주문이 없어야 합니다.");
            assertTrue(orderQueueManager.getMarketSellOrderQueue().isEmpty(), "남은 시장가 매도 주문이 없어야 합니다.");
        } finally {
            // 테스트 종료 시 무한루프 중지
            tradeBatchProcessor.shutdown();
        }
    }

    @DisplayName("시장가매수-지정가매도 매도부분체결")
    @Test
    public void testMarketToLimitPartialTrade1() {
        String ticker = "BTC";

        BuyOrder buyOrder = BuyOrder.createMarketBuyOrder(ticker, 1, 130_000_000.0, LocalDateTime.now(), false);
        SellOrder sellOrder = SellOrder.createLimitSellOrder(ticker, 2, 10.0, 130_000_000.0, LocalDateTime.now(), false);

        buyOrderRepository.save(buyOrder);
        sellOrderRepository.save(sellOrder);

        List<TradeQueueManager> tradeQueueManagers = tradeBatchProcessor.getTradeQueueManagers();
        OrderQueueManager orderQueueManager = tradeQueueManagers.getFirst().getOrderQueueManager();
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
            assertNotNull(byBuyUserIdAndTicker, "매수인의 거래 내역이 null이면 안됩니다");
            assertEquals(1, byBuyUserIdAndTicker.size(), "매수인의 거래 내역이 정확히 1개여야 합니다");

            assertNotNull(bySellUserIdAndTicker, "매도인의 거래 내역이 null이면 안됩니다");
            assertEquals(1, bySellUserIdAndTicker.size(), "매도인의 거래 내역이 정확히 1개여야 합니다");

            assertEquals(byBuyUserIdAndTicker.getFirst().getTradeId(),
                    bySellUserIdAndTicker.getFirst().getTradeId(),
                    "매수인와 매도인의 거래 내역은 동일한 거래를 가리켜야 합니다"
            );

            assertTrue(orderQueueManager.getMarketBuyOrderQueue().isEmpty(), "남은 시장가 매수 주문이 없어야 합니다.");
            assertEquals(1, orderQueueManager.getLimitSellOrderQueue().size(), "지정가 매도 주문이 1개 남아있어야 합니다.");
        } finally {
            // 테스트 종료 시 무한루프 중지
            tradeBatchProcessor.shutdown();
        }
    }

    @DisplayName("시장가매수-지정가매도 매수부분체결")
    @Test
    public void testMarketToLimitPartialTrade2() {
        String ticker = "BTC";

        BuyOrder buyOrder = BuyOrder.createMarketBuyOrder(ticker, 1, 1_300_000_000.0, LocalDateTime.now(), false);
        SellOrder sellOrder = SellOrder.createLimitSellOrder(ticker, 2, 1.0, 130_000_000.0, LocalDateTime.now(), false);

        buyOrderRepository.save(buyOrder);
        sellOrderRepository.save(sellOrder);

        List<TradeQueueManager> tradeQueueManagers = tradeBatchProcessor.getTradeQueueManagers();
        OrderQueueManager orderQueueManager = tradeQueueManagers.getFirst().getOrderQueueManager();
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
            assertNotNull(byBuyUserIdAndTicker, "매수인의 거래 내역이 null이면 안됩니다");
            assertEquals(1, byBuyUserIdAndTicker.size(), "매수인의 거래 내역이 정확히 1개여야 합니다");

            assertNotNull(bySellUserIdAndTicker, "매도인의 거래 내역이 null이면 안됩니다");
            assertEquals(1, bySellUserIdAndTicker.size(), "매도인의 거래 내역이 정확히 1개여야 합니다");

            assertEquals(byBuyUserIdAndTicker.getFirst().getTradeId(),
                    bySellUserIdAndTicker.getFirst().getTradeId(),
                    "매수인와 매도인의 거래 내역은 동일한 거래를 가리켜야 합니다"
            );

            assertEquals(1, orderQueueManager.getMarketBuyOrderQueue().size(), "시장가 매수 주문이 1개 남아있어야 합니다.");
            assertTrue(orderQueueManager.getLimitSellOrderQueue().isEmpty(), "남은 지정가 매도 주문이 없어야 합니다.");

            assert orderQueueManager.getMarketBuyOrderQueue().peek() != null;
            assertEquals(1_300_000_000.0 - 130_000_000.0,
                    orderQueueManager.getMarketBuyOrderQueue().peek().getRemainingDeposit(),
                    "잔여 예수금이 맞지 않습니다.");
            logger.debug("잔여 예수금 : {}", orderQueueManager.getMarketBuyOrderQueue().peek().getRemainingDeposit());
        } finally {
            // 테스트 종료 시 무한루프 중지
            tradeBatchProcessor.shutdown();
        }
    }

    @DisplayName("지정가매수-시장가매도 매수부분체결")
    @Test
    public void testMarketToLimitPartialTrade3() {
        String ticker = "BTC";

        BuyOrder buyOrder = BuyOrder.createLimitBuyOrder(ticker, 1, 10.0, 130_000_000.0, LocalDateTime.now(), false);
        SellOrder sellOrder = SellOrder.createMarketSellOrder(ticker, 2, 1.0, LocalDateTime.now(), false);

        buyOrderRepository.save(buyOrder);
        sellOrderRepository.save(sellOrder);

        List<TradeQueueManager> tradeQueueManagers = tradeBatchProcessor.getTradeQueueManagers();
        OrderQueueManager orderQueueManager = tradeQueueManagers.getFirst().getOrderQueueManager();
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
            assertNotNull(byBuyUserIdAndTicker, "매수인의 거래 내역이 null이면 안됩니다");
            assertEquals(1, byBuyUserIdAndTicker.size(), "매수인의 거래 내역이 정확히 1개여야 합니다");

            assertNotNull(bySellUserIdAndTicker, "매도인의 거래 내역이 null이면 안됩니다");
            assertEquals(1, bySellUserIdAndTicker.size(), "매도인의 거래 내역이 정확히 1개여야 합니다");

            assertEquals(byBuyUserIdAndTicker.getFirst().getTradeId(),
                    bySellUserIdAndTicker.getFirst().getTradeId(),
                    "매수인와 매도인의 거래 내역은 동일한 거래를 가리켜야 합니다"
            );

            assertEquals(1, orderQueueManager.getLimitBuyOrderQueue().size(), "지정가 매수 주문이 1개 남아있어야 합니다.");
            assertTrue(orderQueueManager.getMarketSellOrderQueue().isEmpty(), "남은 시장가 매도 주문이 없어야 합니다.");
        } finally {
            // 테스트 종료 시 무한루프 중지
            tradeBatchProcessor.shutdown();
        }
    }

    @DisplayName("지정가매수-시장가매도 매도부분체결")
    @Test
    public void testMarketToLimitPartialTrade4() {
        String ticker = "BTC";

        BuyOrder buyOrder = BuyOrder.createLimitBuyOrder(ticker, 1, 1.0, 130_000_000.0, LocalDateTime.now(), false);
        SellOrder sellOrder = SellOrder.createMarketSellOrder(ticker, 2, 10.0, LocalDateTime.now(), false);

        buyOrderRepository.save(buyOrder);
        sellOrderRepository.save(sellOrder);

        List<TradeQueueManager> tradeQueueManagers = tradeBatchProcessor.getTradeQueueManagers();
        OrderQueueManager orderQueueManager = tradeQueueManagers.getFirst().getOrderQueueManager();
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
            assertNotNull(byBuyUserIdAndTicker, "매수인의 거래 내역이 null이면 안됩니다");
            assertEquals(1, byBuyUserIdAndTicker.size(), "매수인의 거래 내역이 정확히 1개여야 합니다");

            assertNotNull(bySellUserIdAndTicker, "매도인의 거래 내역이 null이면 안됩니다");
            assertEquals(1, bySellUserIdAndTicker.size(), "매도인의 거래 내역이 정확히 1개여야 합니다");

            assertEquals(byBuyUserIdAndTicker.getFirst().getTradeId(),
                    bySellUserIdAndTicker.getFirst().getTradeId(),
                    "매수인와 매도인의 거래 내역은 동일한 거래를 가리켜야 합니다"
            );

            assertTrue(orderQueueManager.getLimitBuyOrderQueue().isEmpty(), "지정가 매수 주문이 1개 남아있어야 합니다.");
            assertEquals(1, orderQueueManager.getMarketSellOrderQueue().size(), "남은 시장가 매도 주문이 없어야 합니다.");
        } finally {
            // 테스트 종료 시 무한루프 중지
            tradeBatchProcessor.shutdown();
        }
    }

    // TODO : 여러 가격에 대한 테스트 진행

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