package com.cleanengine.coin.trade.application;

import com.cleanengine.coin.order.application.queue.OrderQueueManager;
import com.cleanengine.coin.order.domain.BuyOrder;
import com.cleanengine.coin.order.domain.Order;
import com.cleanengine.coin.order.domain.SellOrder;
import com.cleanengine.coin.trade.entity.Trade;
import com.cleanengine.coin.user.domain.Wallet;

import java.util.concurrent.PriorityBlockingQueue;

public class TradeQueueManager {

    private volatile boolean running = true; // 무한루프 종료 플래그
    private final OrderQueueManager orderQueueManager;
    private final TradeService tradeService;

    public TradeQueueManager(OrderQueueManager orderQueueManager, TradeService tradeService) {
        this.orderQueueManager = orderQueueManager;
        this.tradeService = tradeService;
    }

    public OrderQueueManager getOrderQueueManager() {
        return orderQueueManager;
    }

    public void doTrade() {
        /*
          문제 - 큐가 비어있는데 굳이 일을해야할까?
             - 큐가 안비어있을떄는 특정 스레드를 호출해서 일을 시키고, 아니면 스레드 블락킹(대기큐)
             -> BQ 메커니즘
             (참고) queueManager.getMarketSellOrderQueue().notify();
          BlockingQueue 에 데이터가 없으면 스레드가 블록되는데, 이걸 활용해서 구현?
          시장가 처리는 큐의 데이터 존재 여부를 통해 할 수 있겠지만..
          비어있을 때가 기준이 아닌, 새로운 주문이 요청되었을 때 한 번 순회하는 게 이상적
         */
        while (running) {
            System.out.println("Running in method: " + new Object(){}.getClass().getEnclosingMethod().getName() +
                                ", Thread ID: " + Thread.currentThread().threadId() +
                                ", Thread Name: " + Thread.currentThread().getName());
            try {
                matchOrders();
//                Thread.sleep(1000);
//            } catch (InterruptedException ex) {
//                throw new RuntimeException(ex);
            } catch (Exception e) {
                System.err.println("Error processing trades for " + orderQueueManager.getTicker() + ": " + e.getMessage());
                throw e;
            }
        }
    }

    public void stop() {
        this.running = false; // 무한루프 종료 플래그
    }

    private void matchOrders() {
//        System.out.println("=========================================");
//        System.out.println("MarketSellOrderQueue size: " + orderQueueManager.getMarketSellOrderQueue().size());
//        System.out.println("LimitSellOrderQueue size: " + orderQueueManager.getLimitSellOrderQueue().size());
//        System.out.println("MarketBuyOrderQueue size: " + orderQueueManager.getMarketBuyOrderQueue().size());
//        System.out.println("LimitBuyOrderQueue size: " + orderQueueManager.getLimitBuyOrderQueue().size());
//        System.out.println("=========================================");

        // 시장가 주문을 우선적으로 처리
        // 1. 시장가 매도 주문, 지정가 매수 주문
        while (!orderQueueManager.getMarketSellOrderQueue().isEmpty()) {
            if (orderQueueManager.getLimitBuyOrderQueue().isEmpty()) {
                break;
            }
            SellOrder marketSellOrder = orderQueueManager.getMarketSellOrderQueue().peek();
            matchWithLimitBuyOrder(marketSellOrder);
        }

        // 2. 시장가 매수 주문, 지정가 매도 주문
        while (!orderQueueManager.getMarketBuyOrderQueue().isEmpty()) {
            if (orderQueueManager.getLimitSellOrderQueue().isEmpty()) {
                break;
            }
            BuyOrder marketBuyOrder = orderQueueManager.getMarketBuyOrderQueue().peek();
            matchWithLimitSellOrder(marketBuyOrder);
        }

        // 3. 지정가 주문
        matchBetweenLimitOrders();
    }

    private void matchWithLimitBuyOrder(SellOrder sellOrder) {
        PriorityBlockingQueue<BuyOrder> limitBuyQueue = orderQueueManager.getLimitBuyOrderQueue();
        if (!limitBuyQueue.isEmpty()) {
            BuyOrder buyOrder = limitBuyQueue.peek();
            executeTrade(buyOrder, sellOrder);
        }
    }

    private void matchWithLimitSellOrder(BuyOrder buyOrder) {
        PriorityBlockingQueue<SellOrder> limitSellQueue = orderQueueManager.getLimitSellOrderQueue();
        if (!limitSellQueue.isEmpty()) {
            SellOrder sellOrder = limitSellQueue.peek();
            executeTrade(buyOrder, sellOrder);
        }
    }

    private void matchBetweenLimitOrders() {
        PriorityBlockingQueue<SellOrder> limitSellQueue = orderQueueManager.getLimitSellOrderQueue();
        PriorityBlockingQueue<BuyOrder> limitBuyQueue = orderQueueManager.getLimitBuyOrderQueue();

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

    protected void executeTrade(BuyOrder buyOrder, SellOrder sellOrder) {
//        System.out.println("Trading... " + buyOrder.getTicker() + " - 구매자:" + buyOrder.getId() + ", 판매자:" + sellOrder.getId());
        // 주문 관련 처리를 먼저 해서 동기화 이슈 가능성을 최소로 하고,
        // 체결 내역 처리는 별도 스레드로 분리하는 게 낫겠다
        double tradedSize;
        double tradedPrice;
        double totalTradedPrice;

        if (buyOrder.getIsMarketOrder()) {
            tradedPrice = sellOrder.getPrice();
            if (buyOrder.getRemainingDeposit() >= tradedPrice * sellOrder.getRemainingSize()) { // 매수 잔여예수금이 매도 잔여량보다 크거나 같은 경우 (매수 부분체결 or 완전체결, 매도 완전체결)
                tradedSize = sellOrder.getRemainingSize();
            } else {
                tradedSize = buyOrder.getRemainingDeposit() / tradedPrice;
            }
        } else if (sellOrder.getIsMarketOrder()) {
            tradedPrice = buyOrder.getPrice();
            tradedSize = Math.min(sellOrder.getRemainingSize(), buyOrder.getRemainingSize());
        } else {
            tradedPrice = getTradedUnitPrice(buyOrder, sellOrder);
            tradedSize = Math.min(buyOrder.getRemainingSize(), sellOrder.getRemainingSize());
        }

        if (tradedSize <= 0) {
            return;
        }
        totalTradedPrice = tradedPrice * tradedSize;

        // 주문 잔여수량, 잔여금액 감소
        buyOrder.decreaseRemainingDeposit(totalTradedPrice);
        buyOrder.decreaseRemainingSize(tradedSize);
        sellOrder.decreaseRemainingSize(tradedSize);
        tradeService.saveOrder(buyOrder);
        tradeService.saveOrder(sellOrder);

        // 주문 완전체결 처리(잔여금액 or 잔여수량이 0)
        this.removeCompletedBuyOrder(buyOrder);
        this.removeCompletedSellOrder(sellOrder);

        // 예수금 처리
        //   - 매수 잔여금액 반환
        if (buyOrder.getPrice() >= tradedPrice) {
            double totalRefundAmount = (buyOrder.getPrice() - tradedPrice) * tradedSize;
            if (totalRefundAmount > 0.0)
                tradeService.increaseAccountCash(buyOrder, totalRefundAmount);
        }
        //   - 매도 예수금 처리
        tradeService.increaseAccountCash(sellOrder, totalTradedPrice);
        
        // 지갑 누적계산
        Wallet buyerWallet = tradeService.findWalletByUserIdAndTicker(buyOrder, orderQueueManager.getTicker());
        double updatedBuySize = buyerWallet.getSize() + tradedSize;
        double currentBuyPrice = buyerWallet.getBuyPrice() != null ? buyerWallet.getBuyPrice() : 0.0;
        double updatedBuyPrice = ((currentBuyPrice * buyerWallet.getSize()) + totalTradedPrice) / updatedBuySize;
        buyerWallet.setSize(updatedBuySize);
        buyerWallet.setBuyPrice(updatedBuyPrice);
        // TODO : ROI 계산
        tradeService.saveWallet(buyerWallet);

        // 매도 시에는 평단가 변동 없음
        Wallet sellerWallet = tradeService.findWalletByUserIdAndTicker(sellOrder, orderQueueManager.getTicker());
        double updatedSellSize = sellerWallet.getSize() - tradedSize;
        sellerWallet.setSize(updatedSellSize);
        tradeService.saveWallet(sellerWallet);

        // 체결내역 저장
        Trade newTrade = createNewTrade(buyOrder, sellOrder, tradedSize, tradedPrice);
        tradeService.saveTrade(newTrade);

        // TODO : 호가 조회를 위한 Order Service 메서드 호출
    }

    private void removeCompletedBuyOrder(BuyOrder order) {
        boolean isOrderCompleted = order.getRemainingSize() <= 0 ||
                                   order.getRemainingDeposit() <= 0;

        if (isOrderCompleted) {
            PriorityBlockingQueue<? extends Order> orderQueue = order.getIsMarketOrder()
                    ? orderQueueManager.getMarketBuyOrderQueue()
                    : orderQueueManager.getLimitBuyOrderQueue();
            orderQueue.remove(order);
        }
    }

    private void removeCompletedSellOrder(SellOrder order) {
        boolean isOrderCompleted = order.getRemainingSize() <= 0;

        if (isOrderCompleted) {
            PriorityBlockingQueue<? extends Order> orderQueue = order.getIsMarketOrder()
                    ? orderQueueManager.getMarketSellOrderQueue()
                    : orderQueueManager.getLimitSellOrderQueue();
            orderQueue.remove(order);
        }
    }

    private static double getTradedUnitPrice(BuyOrder buyOrder, SellOrder sellOrder) {
        // 주문 시간을 비교하여 먼저 들어온 주문의 가격으로 거래
        if (buyOrder.getCreatedAt().isBefore(sellOrder.getCreatedAt())) {
            return buyOrder.getPrice();
        } else {
            return sellOrder.getPrice();
        }
    }

    private Trade createNewTrade(BuyOrder buyOrder, SellOrder sellOrder, double tradeSize, Double tradePrice) {
        Trade trade = new Trade();
        trade.setTicker(orderQueueManager.getTicker());
        trade.setBuyUserId(buyOrder.getUserId());
        trade.setSellUserId(sellOrder.getUserId());
        trade.setPrice(tradePrice);
        trade.setSize(tradeSize);
        return trade;
    }
}