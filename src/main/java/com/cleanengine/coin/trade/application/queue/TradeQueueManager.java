package com.cleanengine.coin.trade.application.queue;

import com.cleanengine.coin.order.application.queue.OrderQueueManager;
import com.cleanengine.coin.order.application.queue.OrderQueueManagerPool;
import com.cleanengine.coin.order.domain.BuyOrder;
import com.cleanengine.coin.order.domain.SellOrder;
import com.cleanengine.coin.trade.application.TradeService;
import com.cleanengine.coin.trade.entity.Trade;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.PriorityBlockingQueue;

@Component
public class TradeQueueManager {

    private final OrderQueueManagerPool orderQueueManagerPool;
    private final String ticker;
    private final TradeService tradeService;

    public TradeQueueManager(OrderQueueManagerPool orderQueueManagerPool, String ticker, TradeService tradeService) {
        this.orderQueueManagerPool = orderQueueManagerPool;
        this.ticker = ticker;
        this.tradeService = tradeService;
    }

    public void doTrade() {
        OrderQueueManager queueManager = orderQueueManagerPool.getOrderQueueManager(ticker);
        matchOrders(queueManager);
    }

    private void matchOrders(OrderQueueManager queueManager) {
        // 시장가 주문을 우선적으로 처리
        // 1. 시장가 매도 주문, 지정가 매수 주문
        /**
         * 문제 - 큐가 비어있는데 굳이 일을해야할까?
         *    - 큐가 안비어있을떄는 특정 스레드를 호출해서 일을 시키고, 아니면 스레드 블락킹(대기큐)
         *    -> BQ 메커니즘
         *    (참고) queueManager.getMarketSellOrderQueue().notify();
         */

        while (!queueManager.getMarketSellOrderQueue().isEmpty()) {
            if (queueManager.getLimitBuyOrderQueue().isEmpty()) {
                break;
            }
            SellOrder marketSellOrder = queueManager.getMarketSellOrderQueue().peek();
            matchWithLimitBuyOrder(marketSellOrder, queueManager);
        }

        // 2. 시장가 매수 주문, 지정가 매도 주문
        while (!queueManager.getMarketBuyOrderQueue().isEmpty()) {
            if (queueManager.getLimitSellOrderQueue().isEmpty()) {
                break;
            }
            BuyOrder marketBuyOrder = queueManager.getMarketBuyOrderQueue().peek();
            matchWithLimitSellOrder(marketBuyOrder, queueManager);
        }

        // 3. 지정가 주문
        matchLimitOrders(queueManager);
    }

    private boolean matchWithLimitBuyOrder(SellOrder sellOrder, OrderQueueManager queueManager) {
        PriorityBlockingQueue<BuyOrder> limitBuyQueue = queueManager.getLimitBuyOrderQueue();
        if (!limitBuyQueue.isEmpty()) {
            BuyOrder buyOrder = limitBuyQueue.peek();
            return executeTrade(buyOrder, sellOrder);
        }
        return false;
    }

    private boolean matchWithLimitSellOrder(BuyOrder buyOrder, OrderQueueManager queueManager) {
        PriorityBlockingQueue<SellOrder> limitSellQueue = queueManager.getLimitSellOrderQueue();
        if (!limitSellQueue.isEmpty()) {
            SellOrder sellOrder = limitSellQueue.peek();
            return executeTrade(buyOrder, sellOrder);
        }
        return false;
    }

    private void matchLimitOrders(OrderQueueManager queueManager) {
        PriorityBlockingQueue<SellOrder> limitSellQueue = queueManager.getLimitSellOrderQueue();
        PriorityBlockingQueue<BuyOrder> limitBuyQueue = queueManager.getLimitBuyOrderQueue();

        while (!limitSellQueue.isEmpty() && !limitBuyQueue.isEmpty()) {
            SellOrder sellOrder = limitSellQueue.peek();
            BuyOrder buyOrder = limitBuyQueue.peek();

            if (!canMatch(buyOrder, sellOrder)) {
                break; // 가격 조건 불일치로 매칭 불가
            }

            executeTrade(buyOrder, sellOrder);
        }
    }

    private boolean canMatch(BuyOrder buyOrder, SellOrder sellOrder) {
        return buyOrder.getPrice() >= sellOrder.getPrice();
    }

    @Transactional
    protected boolean executeTrade(BuyOrder buyOrder, SellOrder sellOrder) {
        double tradeSize = Math.min(buyOrder.getRemainingSize(), sellOrder.getRemainingSize());
        if (tradeSize <= 0) {
            return false;
        }

        Trade newTrade = createNewTrade(buyOrder, sellOrder, tradeSize);
        tradeService.saveTrade(newTrade);

        buyOrder.setSize(buyOrder.getSize() - tradeSize);
        sellOrder.setSize(sellOrder.getSize() - tradeSize);

        if (buyOrder.getSize() <= 0) {
            orderQueueManagerPool.getOrderQueueManager(ticker).getLimitBuyOrderQueue().poll();
        }
        if (sellOrder.getSize() <= 0) {
            orderQueueManagerPool.getOrderQueueManager(ticker).getLimitSellOrderQueue().poll();
        }

        return true;
    }

    private Trade createNewTrade(BuyOrder buyOrder, SellOrder sellOrder, double tradeSize) {
        double tradePrice;
        // 주문 시간을 비교하여 먼저 들어온 주문의 가격으로 거래
        if (buyOrder.getCreatedAt().isBefore(sellOrder.getCreatedAt())) {
            // 매수 주문이 먼저 들어온 경우 - 매수 가격으로 거래
            tradePrice = buyOrder.getPrice();
        } else {
            // 매도 주문이 먼저 들어온 경우 - 매도 가격으로 거래
            tradePrice = sellOrder.getPrice();
        }

        // TODO : Lock Orders.. synchronized?

        Trade trade = new Trade();
        trade.setTicker(ticker);
        trade.setBuyUserId(buyOrder.getUserId());
        trade.setSellUserId(sellOrder.getUserId());
        trade.setPrice(tradePrice);
        trade.setSize(tradeSize);
        return trade;
    }
}