package com.cleanengine.coin.order.application.queue;

import com.cleanengine.coin.order.domain.BuyOrder;
import com.cleanengine.coin.order.domain.Order;
import com.cleanengine.coin.order.domain.SellOrder;
import static com.cleanengine.coin.order.domain.tool.SellOrderGenerator.LimitSellOrderGenerator.*;
import static com.cleanengine.coin.order.domain.tool.SellOrderGenerator.MarketSellOrderGenerator.*;
import static com.cleanengine.coin.order.domain.tool.BuyOrderGenerator.LimitBuyOrderGenerator.*;
import static com.cleanengine.coin.order.domain.tool.BuyOrderGenerator.MarketBuyOrderGenerator.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;


import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("OrderQueueManager 테스트")
public class OrderQueueManagerTest {
    private static final String TICKER = "BTC";

    private OrderQueueManager orderQueueManager;

    @BeforeEach
    public void setUp(){
        orderQueueManager = new OrderQueueManager(TICKER);
    }

    private void addOrderToQueueManager(Order order){
        orderQueueManager.addOrder(order);
    }

    private void addBuyOrdersToQueueManager(List<BuyOrder> orders){
        for(Order order : orders){
            orderQueueManager.addOrder(order);
        }
    }

    private void addSellOrdersToQueueManager(List<SellOrder> orders){
        for(Order order : orders){
            orderQueueManager.addOrder(order);
        }
    }

    @Test
    @DisplayName("매수지정가 주문을 삽입시 매수지정가 큐에 올바르게 삽입됨")
    void addLimitBuyOrder_limitBuyOrderQueueSizeIncreased() {
        BuyOrder limitBuyOrder = createLimitBuyOrderWithRandomPrice();

        addOrderToQueueManager(limitBuyOrder);

        PriorityBlockingQueue<BuyOrder> limitBuyOrderQueue = orderQueueManager.getLimitBuyOrderQueue();

        assertEquals(1, limitBuyOrderQueue.size());
        assertEquals(limitBuyOrder, limitBuyOrderQueue.poll());
    }

    @Test
    @DisplayName("매수시장가 주문을 삽입시 매수시장가 큐에 올바르게 삽입됨")
    void addMarketBuyOrder_marketBuyOrderQueueSizeIncreased() {
        BuyOrder marketBuyOrder = createMarketBuyOrderWithRandomPrice();

        addOrderToQueueManager(marketBuyOrder);

        PriorityBlockingQueue<BuyOrder> marketBuyOrderQueue = orderQueueManager.getMarketBuyOrderQueue();

        assertEquals(1, marketBuyOrderQueue.size());
        assertEquals(marketBuyOrder, marketBuyOrderQueue.poll());
    }

    @Test
    @DisplayName("매도지정가 주문을 삽입시 매도지정가 큐에 올바르게 삽입됨")
    void addLimitSellOrder_limitSellOrderQueueSizeIncreased() {
        SellOrder limitSellOrder = createLimitSellOrderWithRandomPrice();

        addOrderToQueueManager(limitSellOrder);

        PriorityBlockingQueue<SellOrder> limitSellOrderQueue = orderQueueManager.getLimitSellOrderQueue();

        assertEquals(1, limitSellOrderQueue.size());
        assertEquals(limitSellOrder, limitSellOrderQueue.poll());
    }

    @Test
    @DisplayName("매도시장가 주문을 삽입시 매도시장가 큐에 올바르게 삽입됨")
    void addMarketSellOrder_marketSellOrderQueueSizeIncreased() {
        SellOrder marketSellOrder = createMarketSellOrderWithCreatedTime(LocalDateTime.now());

        addOrderToQueueManager(marketSellOrder);

        PriorityBlockingQueue<SellOrder> marketSellOrderQueue = orderQueueManager.getMarketSellOrderQueue();

        assertEquals(1, marketSellOrderQueue.size());
        assertEquals(marketSellOrder, marketSellOrderQueue.poll());
    }

    @Test
    @DisplayName("매수지정가 주문을 가격이 높은 순서대로 큐에 삽입시 가격이 높은 순서대로 정렬됨")
    void addLimitBuyOrders_higherPriceFirst_sortedDescending() {
        List<BuyOrder> limitBuyOrdersAsc = createLimitBuyOrdersWithPrices
                        (1000.0, 500.0, 100.0);
        List<BuyOrder> limitBuyOrdersDesc = limitBuyOrdersAsc.reversed();

        addBuyOrdersToQueueManager(limitBuyOrdersDesc);

        PriorityBlockingQueue<BuyOrder> limitBuyOrderQueue = orderQueueManager.getLimitBuyOrderQueue();

        assertEquals(limitBuyOrderQueue.poll().getPrice(), 1000.0);
        assertEquals(limitBuyOrderQueue.poll().getPrice(), 500.0);
        assertEquals(limitBuyOrderQueue.poll().getPrice(), 100.0);
    }

    @Test
    @DisplayName("매수지정가 주문을 가격이 작은 순서대로 큐에 삽입시 가격이 높은 순서대로 정렬됨")
    void addLimitBuyOrders_lowerPriceFirst_sortedDescending() {
        List<BuyOrder> limitBuyOrdersAsc = createLimitBuyOrdersWithPrices
                        (100.0, 500.0, 1000.0);

        addBuyOrdersToQueueManager(limitBuyOrdersAsc);

        PriorityBlockingQueue<BuyOrder> limitBuyOrderQueue = orderQueueManager.getLimitBuyOrderQueue();

        assertEquals(limitBuyOrderQueue.poll().getPrice(), 1000.0);
        assertEquals(limitBuyOrderQueue.poll().getPrice(), 500.0);
        assertEquals(limitBuyOrderQueue.poll().getPrice(), 100.0);
    }

    @Test
    @DisplayName("가격이 같은 매수지정가 주문을 시간이 빠른 순서대로 큐에 삽입시 시간이 빠른 순서대로 정렬됨")
    void addLimitBuyOrdersSamePrice_fasterTimeFirst_sortedFaster() {
        List<BuyOrder> limitBuyOrders =
                createLimitBuyOrdersWithDifferentCreatedTimesAsc();

        addBuyOrdersToQueueManager(limitBuyOrders);

        PriorityBlockingQueue<BuyOrder> limitBuyOrderQueue = orderQueueManager.getLimitBuyOrderQueue();

        assertEquals(limitBuyOrderQueue.poll().getCreatedAt().getYear(), 2025);
        assertEquals(limitBuyOrderQueue.poll().getCreatedAt().getYear(), 2026);
        assertEquals(limitBuyOrderQueue.poll().getCreatedAt().getYear(), 2027);
        assertEquals(limitBuyOrderQueue.poll().getCreatedAt().getYear(), 2028);
    }

    @Test
    @DisplayName("가격이 같은 매수지정가 주문을 시간이 느린 순서대로 큐에 삽입시 시간이 빠른 순서대로 정렬됨")
    void addLimitBuyOrdersSamePrice_slowerTimeFirst_sortedFaster() {
        List<BuyOrder> limitBuyOrders =
                createLimitBuyOrdersWithDifferentCreatedTimesAsc();
        List<BuyOrder> limitBuyOrdersDesc =
                limitBuyOrders.reversed();

        addBuyOrdersToQueueManager(limitBuyOrdersDesc);

        PriorityBlockingQueue<BuyOrder> limitBuyOrderQueue = orderQueueManager.getLimitBuyOrderQueue();

        assertEquals(limitBuyOrderQueue.poll().getCreatedAt().getYear(), 2025);
        assertEquals(limitBuyOrderQueue.poll().getCreatedAt().getYear(), 2026);
        assertEquals(limitBuyOrderQueue.poll().getCreatedAt().getYear(), 2027);
        assertEquals(limitBuyOrderQueue.poll().getCreatedAt().getYear(), 2028);
    }

    @Test
    @DisplayName("매도지정가 주문을 가격이 낮은 순서대로 큐에 삽입시 가격이 낮은 순서대로 정렬됨")
    void addLimitSellOrders_lowerPriceFirst_sortedAscending() {
        List<SellOrder> limitSellOrders =
                createLimitSellOrdersWithPrices
                        (100.0, 500.0, 1000.0);

        addSellOrdersToQueueManager(limitSellOrders);

        PriorityBlockingQueue<SellOrder> limitSellOrderQueue = orderQueueManager.getLimitSellOrderQueue();

        assertEquals(limitSellOrderQueue.poll().getPrice(), 100.0);
        assertEquals(limitSellOrderQueue.poll().getPrice(), 500.0);
        assertEquals(limitSellOrderQueue.poll().getPrice(), 1000.0);
    }

    @Test
    @DisplayName("매도지정가 주문을 가격이 높은 순서대로 큐에 삽입시 가격이 낮은 순서대로 정렬됨")
    void addLimitSellOrders_highestPriceFirst_sortedAscending() {
        List<SellOrder> limitSellOrders =
                createLimitSellOrdersWithPrices
                        (1000.0, 500.0, 100.0);

        addSellOrdersToQueueManager(limitSellOrders);

        PriorityBlockingQueue<SellOrder> limitSellOrderQueue = orderQueueManager.getLimitSellOrderQueue();

        assertEquals(limitSellOrderQueue.poll().getPrice(), 100.0);
        assertEquals(limitSellOrderQueue.poll().getPrice(), 500.0);
        assertEquals(limitSellOrderQueue.poll().getPrice(), 1000.0);
    }

    @Test
    @DisplayName("가격이 같은 매도지정가 주문을 시간이 빠른 순서대로 큐에 삽입시 시간이 빠른 순서대로 정렬됨")
    void addLimitSellOrdersSamePrice_fasterTimeFirst_sortedFaster() {
        List<SellOrder> limitSellOrders =
                createLimitSellOrdersWithDifferentCreatedTimesAsc();

        addSellOrdersToQueueManager(limitSellOrders);

        PriorityBlockingQueue<SellOrder> limitSellOrderQueue = orderQueueManager.getLimitSellOrderQueue();

        assertEquals(limitSellOrderQueue.poll().getCreatedAt().getYear(), 2025);
        assertEquals(limitSellOrderQueue.poll().getCreatedAt().getYear(), 2026);
        assertEquals(limitSellOrderQueue.poll().getCreatedAt().getYear(), 2027);
        assertEquals(limitSellOrderQueue.poll().getCreatedAt().getYear(), 2028);
    }

    @Test
    @DisplayName("가격이 같은 매도지정가 주문을 시간이 느린 순서대로 큐에 삽입시 시간이 빠른 순서대로 정렬됨")
    void addLimitSellOrdersSamePrice_slowerTimeFirst_sortedFaster() {
        List<SellOrder> limitSellOrders =
                createLimitSellOrdersWithDifferentCreatedTimesAsc();
        List<SellOrder> limitSellOrdersDesc =
                limitSellOrders.reversed();

        addSellOrdersToQueueManager(limitSellOrdersDesc);

        PriorityBlockingQueue<SellOrder> limitSellOrderQueue = orderQueueManager.getLimitSellOrderQueue();

        assertEquals(limitSellOrderQueue.poll().getCreatedAt().getYear(), 2025);
        assertEquals(limitSellOrderQueue.poll().getCreatedAt().getYear(), 2026);
        assertEquals(limitSellOrderQueue.poll().getCreatedAt().getYear(), 2027);
        assertEquals(limitSellOrderQueue.poll().getCreatedAt().getYear(), 2028);
    }

    @Test
    @DisplayName("가격이 다른 매수시장가 주문을 높은 가격 순서대로 삽입시 시간이 빠른 순서대로 정렬됨")
    void addMarketBuyOrders_highestPriceFirst_sortedFaster() {
        List<BuyOrder> marketBuyOrders =
                List.of(createMarketBuyOrderWithPriceAndCreatedTime(1000.0, LocalDateTime.of(2027, 5, 9, 10, 18, 0)),
                        createMarketBuyOrderWithPriceAndCreatedTime(500.0, LocalDateTime.of(2026, 5, 9, 10, 18, 0)),
                        createMarketBuyOrderWithPriceAndCreatedTime(100.0, LocalDateTime.of(2025, 5, 9, 10, 18, 0)));

        addBuyOrdersToQueueManager(marketBuyOrders);

        PriorityBlockingQueue<BuyOrder> marketBuyOrderQueue = orderQueueManager.getMarketBuyOrderQueue();

        assertEquals(marketBuyOrderQueue.poll().getCreatedAt().getYear(), 2025);
        assertEquals(marketBuyOrderQueue.poll().getCreatedAt().getYear(), 2026);
        assertEquals(marketBuyOrderQueue.poll().getCreatedAt().getYear(), 2027);
    }

    @Test
    @DisplayName("매도시장가 주문을 시간이 느린 순서대로 삽입시 시간이 빠른 순서대로 정렬됨")
    void addMarketSellOrders_slowerTimeFirst_sortedFaster() {
        List<SellOrder> marketSellOrders =
                createMarketSellOrdersWithDifferentCreatedTimesAsc();
        List<SellOrder> marketSellOrdersDesc =
                marketSellOrders.reversed();

        addSellOrdersToQueueManager(marketSellOrdersDesc);

        PriorityBlockingQueue<SellOrder> marketSellOrderQueue = orderQueueManager.getMarketSellOrderQueue();

        assertEquals(marketSellOrderQueue.poll().getCreatedAt().getYear(), 2025);
        assertEquals(marketSellOrderQueue.poll().getCreatedAt().getYear(), 2026);
        assertEquals(marketSellOrderQueue.poll().getCreatedAt().getYear(), 2027);
    }
}
