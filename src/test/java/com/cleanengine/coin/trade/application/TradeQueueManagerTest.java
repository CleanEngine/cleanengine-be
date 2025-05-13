package com.cleanengine.coin.trade.application;

import com.cleanengine.coin.order.application.queue.OrderQueueManager;
import com.cleanengine.coin.order.application.queue.OrderQueueManagerPool;
import com.cleanengine.coin.order.domain.BuyOrder;
import com.cleanengine.coin.order.domain.Order;
import com.cleanengine.coin.order.domain.SellOrder;
import com.cleanengine.coin.order.infra.BuyOrderRepository;
import com.cleanengine.coin.order.infra.SellOrderRepository;
import com.cleanengine.coin.trade.entity.Trade;
import com.cleanengine.coin.trade.infra.TradeRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DisplayName("체결 처리 테스트")
public class TradeQueueManagerTest {

    private static final Logger logger = LoggerFactory.getLogger(TradeQueueManagerTest.class);

    private static TradeBatchProcessor staticTradeBatchProcessor;

    private final double MINIMUM_ORDER_SIZE = 0.00000001;

    @Autowired
    BuyOrderRepository buyOrderRepository;
    @Autowired
    SellOrderRepository sellOrderRepository;
    @Autowired
    TradeRepository tradeRepository;
    @Autowired
    TradeBatchProcessor tradeBatchProcessor;
    @Autowired
    private OrderQueueManagerPool orderQueueManagerPool;

    private final String ticker = "BTC";

    private void addBuyOrdersToQueueManager(List<BuyOrder> orders){
        if (orders.isEmpty()) return;
        String ticker = orders.getFirst().getTicker();
        OrderQueueManager orderQueueManager = orderQueueManagerPool.getOrderQueueManager(ticker);
        for(com.cleanengine.coin.order.domain.Order order : orders){
            orderQueueManager.addOrder(order);
        }
    }

    private void addSellOrdersToQueueManager(List<SellOrder> orders){
        if (orders.isEmpty()) return;
        String ticker = orders.getFirst().getTicker();
        OrderQueueManager orderQueueManager = orderQueueManagerPool.getOrderQueueManager(ticker);
        for(Order order : orders){
            orderQueueManager.addOrder(order);
        }
    }

    @BeforeEach
    void setUp() {
        if (staticTradeBatchProcessor == null) {
            staticTradeBatchProcessor = tradeBatchProcessor;
        }
        OrderQueueManager orderQueueManager = orderQueueManagerPool.getOrderQueueManager(ticker);
        orderQueueManager.getMarketSellOrderQueue().clear();
        orderQueueManager.getMarketBuyOrderQueue().clear();
        orderQueueManager.getLimitSellOrderQueue().clear();
        orderQueueManager.getLimitBuyOrderQueue().clear();
        tradeRepository.deleteAll();
        buyOrderRepository.deleteAll();
        sellOrderRepository.deleteAll();
    }

    @AfterAll
    static void cleanup() {
        staticTradeBatchProcessor.shutdown();
        // 모든 스레드가 정리될 때까지 잠시 대기
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // TODO : 모든 케이스에서 각 객체의 값까지 정합성이 맞는지 테스트 필요

    @DisplayName("지정가매수-지정가매도 완전체결")
    @Test
    public void testLimitToLimitCompleteTrade() {
        double orderSize = 10.0;
        double price = 130_000_000.0;
        BuyOrder buyOrder = BuyOrder.createLimitBuyOrder(ticker, 1, orderSize, price, LocalDateTime.now(), false);
        SellOrder sellOrder = SellOrder.createLimitSellOrder(ticker, 2, orderSize, price, LocalDateTime.now(), false);

        buyOrderRepository.save(buyOrder);
        sellOrderRepository.save(sellOrder);

        Map<String, TradeQueueManager> tradeQueueManagers = tradeBatchProcessor.getTradeQueueManagers();
        OrderQueueManager orderQueueManager = tradeQueueManagers.get(ticker).getOrderQueueManager();
        orderQueueManager.addOrder(buyOrder);
        orderQueueManager.addOrder(sellOrder);

        // 체결이 완료될 때까지 대기 (최대 3초)
        await()
                .atMost(3, TimeUnit.SECONDS)
                .pollInterval(100, TimeUnit.MILLISECONDS)
                .until(() -> {
                    List<Trade> trades = tradeRepository.findByBuyUserIdAndTicker(1, ticker);
                    return !trades.isEmpty();
                });

        List<Trade> tradeOfBuy = tradeRepository.findByBuyUserIdAndTicker(1, ticker);
        List<Trade> tradeOfSell = tradeRepository.findBySellUserIdAndTicker(2, ticker);
        assertNotNull(tradeOfBuy, "매수인의 거래 내역이 null이면 안됩니다");
        assertEquals(1, tradeOfBuy.size(), "매수인의 거래 내역이 정확히 1개여야 합니다");

        assertNotNull(tradeOfSell, "매도인의 거래 내역이 null이면 안됩니다");
        assertEquals(1, tradeOfSell.size(), "매도인의 거래 내역이 정확히 1개여야 합니다");

        assertEquals(
                tradeOfBuy.getFirst().getTradeId(),
                tradeOfSell.getFirst().getTradeId(),
                "매수인와 매도인의 거래 내역은 동일한 거래를 가리켜야 합니다"
        );

        assertTrue(tradeOfBuy.getFirst().getSize() - orderSize < MINIMUM_ORDER_SIZE, "체결수량과 주문수량은 같아야 합니다.");
        assertTrue(tradeOfBuy.getFirst().getPrice() - price < MINIMUM_ORDER_SIZE, "체결단가와 주문단가는 같아야 합니다.");

        assertTrue(orderQueueManager.getLimitBuyOrderQueue().isEmpty(), "남은 지정가 매수 주문이 없어야 합니다.");
        assertTrue(orderQueueManager.getLimitSellOrderQueue().isEmpty(), "남은 지정가 매도 주문이 없어야 합니다.");

        assertTrue(buyOrder.getRemainingSize() < MINIMUM_ORDER_SIZE, "매수주문의 잔여수량은 없어야 합니다.");
        assertTrue(sellOrder.getRemainingSize() < MINIMUM_ORDER_SIZE, "매도주문의 잔여수량은 없어야 합니다.");
    }

    @DisplayName("지정가매수-지정가매도 매도부분체결")
    @Test
    public void testLimitToLimitPartialTrade1() {
        BuyOrder buyOrder = BuyOrder.createLimitBuyOrder(ticker, 1, 5.0, 130_000_000.0, LocalDateTime.now(), false);
        SellOrder sellOrder = SellOrder.createLimitSellOrder(ticker, 2, 10.0, 130_000_000.0, LocalDateTime.now(), false);

        buyOrderRepository.save(buyOrder);
        sellOrderRepository.save(sellOrder);

        Map<String, TradeQueueManager> tradeQueueManagers = tradeBatchProcessor.getTradeQueueManagers();
        OrderQueueManager orderQueueManager = tradeQueueManagers.get(ticker).getOrderQueueManager();
        orderQueueManager.addOrder(buyOrder);
        orderQueueManager.addOrder(sellOrder);

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
    }

    @DisplayName("지정가매수-지정가매도 매수부분체결")
    @Test
    public void testLimitToLimitPartialTrade2() {
        BuyOrder buyOrder = BuyOrder.createLimitBuyOrder(ticker, 1, 10.0, 130_000_000.0, LocalDateTime.now(), false);
        SellOrder sellOrder = SellOrder.createLimitSellOrder(ticker, 2, 5.0, 130_000_000.0, LocalDateTime.now(), false);

        buyOrderRepository.save(buyOrder);
        sellOrderRepository.save(sellOrder);

        Map<String, TradeQueueManager> tradeQueueManagers = tradeBatchProcessor.getTradeQueueManagers();
        OrderQueueManager orderQueueManager = tradeQueueManagers.get(ticker).getOrderQueueManager();
        orderQueueManager.addOrder(buyOrder);
        orderQueueManager.addOrder(sellOrder);

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
    }

    @DisplayName("시장가매수-지정가매도 완전체결")
    @Test
    public void testMarketToLimitCompleteTrade1() {
        BuyOrder buyOrder = BuyOrder.createMarketBuyOrder(ticker, 1, 1_300_000_000.0, LocalDateTime.now(), false);
        SellOrder sellOrder = SellOrder.createLimitSellOrder(ticker, 2, 10.0, 130_000_000.0, LocalDateTime.now(), false);

        buyOrderRepository.save(buyOrder);
        sellOrderRepository.save(sellOrder);

        Map<String, TradeQueueManager> tradeQueueManagers = tradeBatchProcessor.getTradeQueueManagers();
        OrderQueueManager orderQueueManager = tradeQueueManagers.get(ticker).getOrderQueueManager();
        orderQueueManager.addOrder(buyOrder);
        orderQueueManager.addOrder(sellOrder);

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
    }

    @DisplayName("지정가매수-시장가매도 완전체결")
    @Test
    public void testMarketToLimitCompleteTrade2() {
        BuyOrder buyOrder = BuyOrder.createLimitBuyOrder(ticker, 1, 10.0, 130_000_000.0, LocalDateTime.now(), false);
        SellOrder sellOrder = SellOrder.createMarketSellOrder(ticker, 2, 10.0, LocalDateTime.now(), false);

        buyOrderRepository.save(buyOrder);
        sellOrderRepository.save(sellOrder);

        Map<String, TradeQueueManager> tradeQueueManagers = tradeBatchProcessor.getTradeQueueManagers();
        OrderQueueManager orderQueueManager = tradeQueueManagers.get(ticker).getOrderQueueManager();
        orderQueueManager.addOrder(buyOrder);
        orderQueueManager.addOrder(sellOrder);

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
    }

    @DisplayName("시장가매수-지정가매도 매도부분체결")
    @Test
    public void testMarketToLimitPartialTrade1() {
        BuyOrder buyOrder = BuyOrder.createMarketBuyOrder(ticker, 1, 130_000_000.0, LocalDateTime.now(), false);
        SellOrder sellOrder = SellOrder.createLimitSellOrder(ticker, 2, 10.0, 130_000_000.0, LocalDateTime.now(), false);

        buyOrderRepository.save(buyOrder);
        sellOrderRepository.save(sellOrder);

        Map<String, TradeQueueManager> tradeQueueManagers = tradeBatchProcessor.getTradeQueueManagers();
        OrderQueueManager orderQueueManager = tradeQueueManagers.get(ticker).getOrderQueueManager();
        orderQueueManager.addOrder(buyOrder);
        orderQueueManager.addOrder(sellOrder);

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
    }

    @DisplayName("시장가매수-지정가매도 매수부분체결")
    @Test
    public void testMarketToLimitPartialTrade2() {
        BuyOrder buyOrder = BuyOrder.createMarketBuyOrder(ticker, 1, 1_300_000_000.0, LocalDateTime.now(), false);
        SellOrder sellOrder = SellOrder.createLimitSellOrder(ticker, 2, 1.0, 130_000_000.0, LocalDateTime.now(), false);

        buyOrderRepository.save(buyOrder);
        sellOrderRepository.save(sellOrder);

        Map<String, TradeQueueManager> tradeQueueManagers = tradeBatchProcessor.getTradeQueueManagers();
        OrderQueueManager orderQueueManager = tradeQueueManagers.get(ticker).getOrderQueueManager();
        orderQueueManager.addOrder(buyOrder);
        orderQueueManager.addOrder(sellOrder);

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
    }

    @DisplayName("지정가매수-시장가매도 매수부분체결")
    @Test
    public void testMarketToLimitPartialTrade3() {
        BuyOrder buyOrder = BuyOrder.createLimitBuyOrder(ticker, 1, 10.0, 130_000_000.0, LocalDateTime.now(), false);
        SellOrder sellOrder = SellOrder.createMarketSellOrder(ticker, 2, 1.0, LocalDateTime.now(), false);

        buyOrderRepository.save(buyOrder);
        sellOrderRepository.save(sellOrder);

        Map<String, TradeQueueManager> tradeQueueManagers = tradeBatchProcessor.getTradeQueueManagers();
        OrderQueueManager orderQueueManager = tradeQueueManagers.get(ticker).getOrderQueueManager();
        orderQueueManager.addOrder(buyOrder);
        orderQueueManager.addOrder(sellOrder);

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
    }

    @DisplayName("지정가매수-시장가매도 매도부분체결")
    @Test
    public void testMarketToLimitPartialTrade4() {
        BuyOrder buyOrder = BuyOrder.createLimitBuyOrder(ticker, 1, 1.0, 130_000_000.0, LocalDateTime.now(), false);
        SellOrder sellOrder = SellOrder.createMarketSellOrder(ticker, 2, 10.0, LocalDateTime.now(), false);

        buyOrderRepository.save(buyOrder);
        sellOrderRepository.save(sellOrder);

        Map<String, TradeQueueManager> tradeQueueManagers = tradeBatchProcessor.getTradeQueueManagers();
        OrderQueueManager orderQueueManager = tradeQueueManagers.get(ticker).getOrderQueueManager();
        orderQueueManager.addOrder(buyOrder);
        orderQueueManager.addOrder(sellOrder);

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
    }

//    @DisplayName("여러 지정가매수-지정가매도 완전체결")
//    @Test
//    public void testMultiLimitToLimitCompleteTrade1() {
//        List<BuyOrder> limitBuyOrdersWithDifferentCreatedTimesAsc = createLimitBuyOrdersWithDifferentCreatedTimesAsc();
//        List<BuyOrder> marketBuyOrdersWithDifferentPricesAsc = createMarketBuyOrdersWithDifferentPricesAsc();
//        addBuyOrdersToQueueManager(limitBuyOrdersWithDifferentCreatedTimesAsc);
//        addBuyOrdersToQueueManager(marketBuyOrdersWithDifferentPricesAsc);
//
//        List<SellOrder> limitSellOrdersWithDifferentCreatedTimesAsc = createLimitSellOrdersWithDifferentCreatedTimesAsc();
//        List<SellOrder> marketSellOrdersWithDifferentCreatedTimesAsc = createMarketSellOrdersWithDifferentCreatedTimesAsc();
//        addSellOrdersToQueueManager(limitSellOrdersWithDifferentCreatedTimesAsc);
//        addSellOrdersToQueueManager(marketSellOrdersWithDifferentCreatedTimesAsc);
//
//        OrderQueueManager orderQueueManager = orderQueueManagerPool.getOrderQueueManager("BTC");
//
//
//
//        System.out.println(orderQueueManager);
//        ;
//    }
}