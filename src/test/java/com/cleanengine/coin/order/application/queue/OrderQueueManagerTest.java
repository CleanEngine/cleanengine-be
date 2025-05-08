package com.cleanengine.coin.order.application.queue;

import com.cleanengine.coin.order.domain.BuyOrder;
import com.cleanengine.coin.order.domain.SellOrder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.concurrent.PriorityBlockingQueue;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("OrderQueueManager 테스트")
public class OrderQueueManagerTest {
    private static final String TICKER = "BTC";

    private OrderQueueManager createOrderQueueManager() {
        return new OrderQueueManager(TICKER);
    }

    @Test
    @DisplayName("매수지정가 주문을 삽입시 매수지정가 큐에 올바르게 삽입됨")
    void addLimitBuyOrder_limitBuyOrderQueueSizeIncreased() {
        OrderQueueManager orderQueueManager = createOrderQueueManager();

        BuyOrder limitBuyOrder = BuyOrder.createLimitBuyOrder
                (TICKER, 1, 1.0, 1000.0, LocalDateTime.now(), false);

        orderQueueManager.addOrder(limitBuyOrder);

        PriorityBlockingQueue<BuyOrder> limitBuyOrderQueue = orderQueueManager.getLimitBuyOrderQueue();

        assertEquals(1, limitBuyOrderQueue.size());
        assertEquals(limitBuyOrder, limitBuyOrderQueue.poll());
    }

    @Test
    @DisplayName("매수시장가 주문을 삽입시 매수시장가 큐에 올바르게 삽입됨")
    void addMarketBuyOrder_marketBuyOrderQueueSizeIncreased() {
        OrderQueueManager orderQueueManager = createOrderQueueManager();

        BuyOrder marketBuyOrder = BuyOrder.createMarketBuyOrder
                (TICKER, 1, 1000.0, LocalDateTime.now(), false);

        orderQueueManager.addOrder(marketBuyOrder);

        PriorityBlockingQueue<BuyOrder> marketBuyOrderQueue = orderQueueManager.getMarketBuyOrderQueue();

        assertEquals(1, marketBuyOrderQueue.size());
        assertEquals(marketBuyOrder, marketBuyOrderQueue.poll());
    }

    @Test
    @DisplayName("매도지정가 주문을 삽입시 매도지정가 큐에 올바르게 삽입됨")
    void addLimitSellOrder_limitSellOrderQueueSizeIncreased() {
        OrderQueueManager orderQueueManager = createOrderQueueManager();

        SellOrder limitSellOrder = SellOrder.createLimitSellOrder
                (TICKER, 1, 1.0, 1000.0, LocalDateTime.now(), false);

        orderQueueManager.addOrder(limitSellOrder);

        PriorityBlockingQueue<SellOrder> limitSellOrderQueue = orderQueueManager.getLimitSellOrderQueue();

        assertEquals(1, limitSellOrderQueue.size());
        assertEquals(limitSellOrder, limitSellOrderQueue.poll());
    }

    @Test
    @DisplayName("매도시장가 주문을 삽입시 매도시장가 큐에 올바르게 삽입됨")
    void addMarketSellOrder_marketSellOrderQueueSizeIncreased() {
        OrderQueueManager orderQueueManager = createOrderQueueManager();

        SellOrder marketSellOrder = SellOrder.createMarketSellOrder
                (TICKER, 1, 1.0, LocalDateTime.now(), false);

        orderQueueManager.addOrder(marketSellOrder);

        PriorityBlockingQueue<SellOrder> marketSellOrderQueue = orderQueueManager.getMarketSellOrderQueue();

        assertEquals(1, marketSellOrderQueue.size());
        assertEquals(marketSellOrder, marketSellOrderQueue.poll());
    }

    @Test
    @DisplayName("매수지정가 주문을 가격이 높은 순서대로 큐에 삽입시 가격이 높은 순서대로 정렬됨")
    void addLimitBuyOrders_higherPriceFirst_sortedDescending() {
        OrderQueueManager orderQueueManager = createOrderQueueManager();

        String ticker = TICKER;
        Integer userId = 1;
        LocalDateTime sameTime = LocalDateTime.of(2025, 5, 8, 20, 30, 0);
        Double orderSize = 1.0;

        BuyOrder limitBuyOrderWithHighestPrice =
                BuyOrder.createLimitBuyOrder(ticker, userId, orderSize, 1000.0, sameTime, false);
        BuyOrder limitBuyOrderWithMiddlePrice =
                BuyOrder.createLimitBuyOrder(ticker, userId, orderSize, 500.0, sameTime, false);
        BuyOrder limitBuyOrderWithLowestPrice =
                BuyOrder.createLimitBuyOrder(ticker, userId, orderSize, 100.0, sameTime, false);

        orderQueueManager.addOrder(limitBuyOrderWithHighestPrice);
        orderQueueManager.addOrder(limitBuyOrderWithMiddlePrice);
        orderQueueManager.addOrder(limitBuyOrderWithLowestPrice);

        PriorityBlockingQueue<BuyOrder> limitBuyOrderQueue = orderQueueManager.getLimitBuyOrderQueue();

        assertEquals(limitBuyOrderQueue.poll().getPrice(), 1000.0);
        assertEquals(limitBuyOrderQueue.poll().getPrice(), 500.0);
        assertEquals(limitBuyOrderQueue.poll().getPrice(), 100.0);
    }

    @Test
    @DisplayName("매수지정가 주문을 가격이 작은 순서대로 큐에 삽입시 가격이 높은 순서대로 정렬됨")
    void addLimitBuyOrders_lowerPriceFirst_sortedDescending() {
        OrderQueueManager orderQueueManager = createOrderQueueManager();

        String ticker = TICKER;
        Integer userId = 1;
        LocalDateTime sameTime = LocalDateTime.of(2025, 5, 8, 20, 30, 0);
        Double orderSize = 1.0;

        BuyOrder limitBuyOrderWithLowestPrice =
                BuyOrder.createLimitBuyOrder(ticker, userId, orderSize, 100.0, sameTime, false);
        BuyOrder limitBuyOrderWithMiddlePrice =
                BuyOrder.createLimitBuyOrder(ticker, userId, orderSize, 500.0, sameTime, false);
        BuyOrder limitBuyOrderWithHighestPrice =
                BuyOrder.createLimitBuyOrder(ticker, userId, orderSize, 1000.0, sameTime, false);

        orderQueueManager.addOrder(limitBuyOrderWithLowestPrice);
        orderQueueManager.addOrder(limitBuyOrderWithMiddlePrice);
        orderQueueManager.addOrder(limitBuyOrderWithHighestPrice);

        PriorityBlockingQueue<BuyOrder> limitBuyOrderQueue = orderQueueManager.getLimitBuyOrderQueue();

        assertEquals(limitBuyOrderQueue.poll().getPrice(), 1000.0);
        assertEquals(limitBuyOrderQueue.poll().getPrice(), 500.0);
        assertEquals(limitBuyOrderQueue.poll().getPrice(), 100.0);
    }

    @Test
    @DisplayName("가격이 같은 매수지정가 주문을 시간이 빠른 순서대로 큐에 삽입시 시간이 빠른 순서대로 정렬됨")
    void addLimitBuyOrdersSamePrice_fasterTimeFirst_sortedFaster() {
        OrderQueueManager orderQueueManager = createOrderQueueManager();

        String ticker = TICKER;
        Integer userId = 1;
        Double orderSize = 1.0;
        Double price = 100.0;

        LocalDateTime baseTime = LocalDateTime.of(2025, 5, 8, 20, 30, 0);
        LocalDateTime fastestTime = baseTime.minusMinutes(1);
        LocalDateTime slowestTime = baseTime.plusMinutes(1);

        BuyOrder limitBuyOrderWithFastestTime =
                BuyOrder.createLimitBuyOrder(ticker, userId, orderSize, price, fastestTime, false);
        BuyOrder limitBuyOrderWithBaseTime =
                BuyOrder.createLimitBuyOrder(ticker, userId, orderSize, price, baseTime, false);
        BuyOrder limitBuyOrderWithSlowestTime =
                BuyOrder.createLimitBuyOrder(ticker, userId, orderSize, price, slowestTime, false);

        orderQueueManager.addOrder(limitBuyOrderWithFastestTime);
        orderQueueManager.addOrder(limitBuyOrderWithBaseTime);
        orderQueueManager.addOrder(limitBuyOrderWithSlowestTime);

        PriorityBlockingQueue<BuyOrder> limitBuyOrderQueue = orderQueueManager.getLimitBuyOrderQueue();

        assertEquals(limitBuyOrderQueue.poll().getCreatedAt(), fastestTime);
        assertEquals(limitBuyOrderQueue.poll().getCreatedAt(), baseTime);
        assertEquals(limitBuyOrderQueue.poll().getCreatedAt(), slowestTime);
    }

    @Test
    @DisplayName("가격이 같은 매수지정가 주문을 시간이 느린 순서대로 큐에 삽입시 시간이 빠른 순서대로 정렬됨")
    void addLimitBuyOrdersSamePrice_slowerTimeFirst_sortedFaster() {
        OrderQueueManager orderQueueManager = createOrderQueueManager();

        String ticker = TICKER;
        Integer userId = 1;
        Double orderSize = 1.0;
        Double price = 100.0;

        LocalDateTime baseTime = LocalDateTime.of(2025, 5, 8, 20, 30, 0);
        LocalDateTime fastestTime = baseTime.minusMinutes(1);
        LocalDateTime slowestTime = baseTime.plusMinutes(1);

        BuyOrder limitBuyOrderWithSlowestTime =
                BuyOrder.createLimitBuyOrder(ticker, userId, orderSize, price, slowestTime, false);
        BuyOrder limitBuyOrderWithBaseTime =
                BuyOrder.createLimitBuyOrder(ticker, userId, orderSize, price, baseTime, false);
        BuyOrder limitBuyOrderWithFastestTime =
                BuyOrder.createLimitBuyOrder(ticker, userId, orderSize, price, fastestTime, false);

        orderQueueManager.addOrder(limitBuyOrderWithSlowestTime);
        orderQueueManager.addOrder(limitBuyOrderWithBaseTime);
        orderQueueManager.addOrder(limitBuyOrderWithFastestTime);

        PriorityBlockingQueue<BuyOrder> limitBuyOrderQueue = orderQueueManager.getLimitBuyOrderQueue();

        assertEquals(limitBuyOrderQueue.poll().getCreatedAt(), fastestTime);
        assertEquals(limitBuyOrderQueue.poll().getCreatedAt(), baseTime);
        assertEquals(limitBuyOrderQueue.poll().getCreatedAt(), slowestTime);
    }

    @Test
    @DisplayName("매도지정가 주문을 가격이 낮은 순서대로 큐에 삽입시 가격이 낮은 순서대로 정렬됨")
    void addLimitSellOrders_lowerPriceFirst_sortedAscending() {
        OrderQueueManager orderQueueManager = createOrderQueueManager();

        String ticker = TICKER;
        Integer userId = 1;
        LocalDateTime sameTime = LocalDateTime.of(2025, 5, 8, 20, 30, 0);
        Double orderSize = 1.0;

        SellOrder limitBuyOrderWithLowestPrice =
                SellOrder.createLimitSellOrder(ticker, userId, orderSize, 100.0, sameTime, false);
        SellOrder limitSellOrderWithMiddlePrice =
                SellOrder.createLimitSellOrder(ticker, userId, orderSize, 500.0, sameTime, false);
        SellOrder limitSellOrderWithHighestPrice =
                SellOrder.createLimitSellOrder(ticker, userId, orderSize, 1000.0, sameTime, false);

        orderQueueManager.addOrder(limitBuyOrderWithLowestPrice);
        orderQueueManager.addOrder(limitSellOrderWithMiddlePrice);
        orderQueueManager.addOrder(limitSellOrderWithHighestPrice);

        PriorityBlockingQueue<SellOrder> limitSellOrderQueue = orderQueueManager.getLimitSellOrderQueue();

        assertEquals(limitSellOrderQueue.poll().getPrice(), 100.0);
        assertEquals(limitSellOrderQueue.poll().getPrice(), 500.0);
        assertEquals(limitSellOrderQueue.poll().getPrice(), 1000.0);
    }

    @Test
    @DisplayName("매도지정가 주문을 가격이 높은 순서대로 큐에 삽입시 가격이 낮은 순서대로 정렬됨")
    void addLimitSellOrders_highestPriceFirst_sortedAscending() {
        OrderQueueManager orderQueueManager = createOrderQueueManager();

        String ticker = TICKER;
        Integer userId = 1;
        LocalDateTime sameTime = LocalDateTime.of(2025, 5, 8, 20, 30, 0);
        Double orderSize = 1.0;

        SellOrder limitSellOrderWithHighestPrice =
                SellOrder.createLimitSellOrder(ticker, userId, orderSize, 1000.0, sameTime, false);
        SellOrder limitSellOrderWithMiddlePrice =
                SellOrder.createLimitSellOrder(ticker, userId, orderSize, 500.0, sameTime, false);
        SellOrder limitBuyOrderWithLowestPrice =
                SellOrder.createLimitSellOrder(ticker, userId, orderSize, 100.0, sameTime, false);

        orderQueueManager.addOrder(limitSellOrderWithHighestPrice);
        orderQueueManager.addOrder(limitSellOrderWithMiddlePrice);
        orderQueueManager.addOrder(limitBuyOrderWithLowestPrice);

        PriorityBlockingQueue<SellOrder> limitSellOrderQueue = orderQueueManager.getLimitSellOrderQueue();

        assertEquals(limitSellOrderQueue.poll().getPrice(), 100.0);
        assertEquals(limitSellOrderQueue.poll().getPrice(), 500.0);
        assertEquals(limitSellOrderQueue.poll().getPrice(), 1000.0);
    }

    @Test
    @DisplayName("가격이 같은 매도지정가 주문을 시간이 빠른 순서대로 큐에 삽입시 시간이 빠른 순서대로 정렬됨")
    void addLimitSellOrdersSamePrice_fasterTimeFirst_sortedFaster() {
        OrderQueueManager orderQueueManager = createOrderQueueManager();

        String ticker = TICKER;
        Integer userId = 1;
        Double orderSize = 1.0;
        Double price = 100.0;

        LocalDateTime baseTime = LocalDateTime.of(2025, 5, 8, 20, 30, 0);
        LocalDateTime fastestTime = baseTime.minusMinutes(1);
        LocalDateTime slowestTime = baseTime.plusMinutes(1);

        SellOrder limitSellOrderWithFastestTime =
                SellOrder.createLimitSellOrder(ticker, userId, orderSize, price, fastestTime, false);
        SellOrder limitSellOrderWithBaseTime =
                SellOrder.createLimitSellOrder(ticker, userId, orderSize, price, baseTime, false);
        SellOrder limitSellOrderWithSlowestTime =
                SellOrder.createLimitSellOrder(ticker, userId, orderSize, price, slowestTime, false);

        orderQueueManager.addOrder(limitSellOrderWithFastestTime);
        orderQueueManager.addOrder(limitSellOrderWithBaseTime);
        orderQueueManager.addOrder(limitSellOrderWithSlowestTime);

        PriorityBlockingQueue<SellOrder> limitSellOrderQueue = orderQueueManager.getLimitSellOrderQueue();

        assertEquals(limitSellOrderQueue.poll().getCreatedAt(), fastestTime);
        assertEquals(limitSellOrderQueue.poll().getCreatedAt(), baseTime);
        assertEquals(limitSellOrderQueue.poll().getCreatedAt(), slowestTime);
    }

    @Test
    @DisplayName("가격이 같은 매도지정가 주문을 시간이 느린 순서대로 큐에 삽입시 시간이 빠른 순서대로 정렬됨")
    void addLimitBuyOrdersSamePrice_slowerTime_sortedFaster() {
        OrderQueueManager orderQueueManager = createOrderQueueManager();

        String ticker = TICKER;
        Integer userId = 1;
        Double orderSize = 1.0;
        Double price = 100.0;

        LocalDateTime baseTime = LocalDateTime.of(2025, 5, 8, 20, 30, 0);
        LocalDateTime fastestTime = baseTime.minusMinutes(1);
        LocalDateTime slowestTime = baseTime.plusMinutes(1);

        SellOrder limitSellOrderWithSlowestTime =
                SellOrder.createLimitSellOrder(ticker, userId, orderSize, price, slowestTime, false);
        SellOrder limitSellOrderWithBaseTime =
                SellOrder.createLimitSellOrder(ticker, userId, orderSize, price, baseTime, false);
        SellOrder limitSellOrderWithFastestTime =
                SellOrder.createLimitSellOrder(ticker, userId, orderSize, price, fastestTime, false);

        orderQueueManager.addOrder(limitSellOrderWithSlowestTime);
        orderQueueManager.addOrder(limitSellOrderWithBaseTime);
        orderQueueManager.addOrder(limitSellOrderWithFastestTime);

        PriorityBlockingQueue<SellOrder> limitSellOrderQueue = orderQueueManager.getLimitSellOrderQueue();

        assertEquals(limitSellOrderQueue.poll().getCreatedAt(), fastestTime);
        assertEquals(limitSellOrderQueue.poll().getCreatedAt(), baseTime);
        assertEquals(limitSellOrderQueue.poll().getCreatedAt(), slowestTime);
    }

//    @Test
//    @DisplayName("가격이 다른 매수시장가 주문을 높은 가격 순서대로 삽입시 시간이 빠른 순서대로 정렬됨")
//
}
