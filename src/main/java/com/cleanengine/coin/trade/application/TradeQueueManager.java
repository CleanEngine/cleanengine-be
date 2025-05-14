package com.cleanengine.coin.trade.application;

import com.cleanengine.coin.chart.dto.TradeEventDto;
import com.cleanengine.coin.order.application.queue.OrderQueueManager;
import com.cleanengine.coin.order.domain.BuyOrder;
import com.cleanengine.coin.order.domain.Order;
import com.cleanengine.coin.order.domain.SellOrder;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.PriorityBlockingQueue;

public class TradeQueueManager {

    private static final Logger logger = LoggerFactory.getLogger(TradeQueueManager.class);
    private long lastLogTime = 0;
    private static final long LOG_INTERVAL = 1000;
    private final double MINIMUM_ORDER_SIZE = 0.00000001;

    private volatile boolean running = true; // 무한루프 종료 플래그

    @Getter
    private final OrderQueueManager orderQueueManager;
    private final String ticker;
    private final PriorityBlockingQueue<SellOrder> marketSellOrderQueue;
    private final PriorityBlockingQueue<SellOrder> limitSellOrderQueue;
    private final PriorityBlockingQueue<BuyOrder> marketBuyOrderQueue;
    private final PriorityBlockingQueue<BuyOrder> limitBuyOrderQueue;

    private final TradeService tradeService;

    @Getter
    private final TradeEventDto lastTradeEventDto;

    public TradeQueueManager(OrderQueueManager orderQueueManager, TradeService tradeService) {
        this.orderQueueManager = orderQueueManager;
        this.tradeService = tradeService;
        // TODO : orderQueueManager의 필드를 꺼내서 쓰는 게 맞는 방식인지 고려
        this.ticker = orderQueueManager.getTicker();
        this.marketSellOrderQueue = orderQueueManager.getMarketSellOrderQueue();
        this.limitSellOrderQueue = orderQueueManager.getLimitSellOrderQueue();
        this.marketBuyOrderQueue = orderQueueManager.getMarketBuyOrderQueue();
        this.limitBuyOrderQueue = orderQueueManager.getLimitBuyOrderQueue();

        lastTradeEventDto = new TradeEventDto();
        lastTradeEventDto.setTicker(ticker);
    }

    public void run() {
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
            try {
                Optional<TradePair<Order, Order>> targetTradePair = this.matchOrders();
                if (targetTradePair.isEmpty()) {
                    continue;
                } else {
                    this.executeTrade(targetTradePair.get().getBuyOrder(), targetTradePair.get().getSellOrder());
                }
//                Thread.sleep(10);
//            } catch (InterruptedException e) {
            } catch (Exception e) {
                logger.error("Error processing trades for {}: {}", this.ticker, e.getMessage());
                throw e;
            }
        }
    }

    public void stop() {
        this.running = false; // 무한루프 종료 플래그
    }

    private Optional<TradePair<Order, Order>> matchOrders() {  // 반환값 : 체결여부
        this.writeQueueLog();
        TradePair<Order, Order> targetTradePair = null;

        // 시장가 주문 우선처리
        // TODO : race condition 방지 방안? (isEmpty <-> peek 간격 발생)
        if (hasElement(this.marketSellOrderQueue) && hasElement(this.limitBuyOrderQueue)) {
            // 1. 시장가 매도 주문, 지정가 매수 주문
            SellOrder marketSellOrder = this.marketSellOrderQueue.peek();
            BuyOrder limitBuyOrder = this.limitBuyOrderQueue.peek();
            targetTradePair = new TradePair<>(marketSellOrder, limitBuyOrder);
        } else if (hasElement(this.marketBuyOrderQueue) && hasElement(this.limitSellOrderQueue)) {
            // 2. 시장가 매수 주문, 지정가 매도 주문
            BuyOrder marketBuyOrder = this.marketBuyOrderQueue.peek();
            SellOrder limitSellOrder = this.limitSellOrderQueue.peek();
            targetTradePair = new TradePair<>(marketBuyOrder, limitSellOrder);
        } else if (hasElement(this.limitSellOrderQueue) && hasElement(this.limitBuyOrderQueue)) {
            // 3. 지정가 주문
            targetTradePair = this.matchBetweenLimitOrders();
        }
        return Optional.ofNullable(targetTradePair);
    }

    private void writeQueueLog() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastLogTime > LOG_INTERVAL) {
            logger.debug("주문 큐 - 시장가매도[{}], 지정가매도[{}], 시장가매수[{}], 지정가매수[{}]",
                    this.marketSellOrderQueue.size(),
                    this.limitSellOrderQueue.size(),
                    this.marketBuyOrderQueue.size(),
                    this.limitBuyOrderQueue.size());
            lastLogTime = currentTime;
        }
    }

    private TradePair<Order, Order> matchBetweenLimitOrders() {
        PriorityBlockingQueue<SellOrder> limitSellQueue = this.limitSellOrderQueue;
        PriorityBlockingQueue<BuyOrder> limitBuyQueue = this.limitBuyOrderQueue;

        if (this.hasElement(limitSellQueue) && this.hasElement(limitBuyQueue)) {
            SellOrder sellOrder = limitSellQueue.peek();
            BuyOrder buyOrder = limitBuyQueue.peek();

            if (this.canMatch(buyOrder, sellOrder)) {
                return new TradePair<>(buyOrder, sellOrder);
            } else
                return null;
        } else
            return null;
    }

    private boolean hasElement(PriorityBlockingQueue<? extends Order> queue) {
        return !queue.isEmpty();
    }

    private boolean canMatch(BuyOrder buyOrder, SellOrder sellOrder) {
        if (buyOrder == null || sellOrder == null)
            return false;
        return buyOrder.getPrice() >= sellOrder.getPrice();
    }

    protected void executeTrade(BuyOrder buyOrder, SellOrder sellOrder) {
        this.writeTradingLog(buyOrder, sellOrder);
        // 주문 관련 처리를 먼저 해서 동기화 이슈 가능성을 최소로 하고, 체결 내역 처리는 별도 스레드로 분리하는 게 낫겠다
        double tradedPrice;
        double tradedSize;
        double totalTradedPrice;

        // 체결 단가, 수량 확정
        TradeUnitPriceAndSize tradeUnitPriceAndSize = getTradeUnitPriceAndSize(buyOrder, sellOrder);
        tradedSize = tradeUnitPriceAndSize.tradedSize();
        tradedPrice = tradeUnitPriceAndSize.tradedPrice();
        if (tradedSize < MINIMUM_ORDER_SIZE) {
            return;
        }
        totalTradedPrice = tradedPrice * tradedSize;

        // 주문 잔여수량, 잔여금액 감소
        if (isMarketOrder(buyOrder))
            buyOrder.decreaseRemainingDeposit(totalTradedPrice);
        else
            buyOrder.decreaseRemainingSize(tradedSize);
        sellOrder.decreaseRemainingSize(tradedSize);

        // 주문 완전체결 처리(잔여금액 or 잔여수량이 0)
        this.removeCompletedBuyOrder(buyOrder);
        this.removeCompletedSellOrder(sellOrder);

        // DB 테이블 저장에 걸리는 시간 측정용
        long beforeTime = System.currentTimeMillis();
        tradeService.saveOrder(buyOrder);
        tradeService.saveOrder(sellOrder);
        long afterTime = System.currentTimeMillis();
        logger.debug("주문 테이블에 update하는 데 걸린 시간 : {}ms", afterTime - beforeTime);

        // 예수금 처리
        //   - 매수 잔여금액 반환
        if (isMarketOrder(buyOrder)) {
            ; // TODO : 시장가 거래 시 1원 단위 등 작은 금액이 남을 수도 있는데 처리방안
        } else {
            if (buyOrder.getPrice() - tradedPrice > MINIMUM_ORDER_SIZE) { // 매도 호가보다 높은 가격에 매수를 시도한 경우, 차액 반환
                double totalRefundAmount = (buyOrder.getPrice() - tradedPrice) * tradedSize;
                if (totalRefundAmount > MINIMUM_ORDER_SIZE)
                    tradeService.increaseAccountCash(buyOrder, totalRefundAmount);
            }
        }

        //   - 매도 예수금 처리
        tradeService.increaseAccountCash(sellOrder, totalTradedPrice);

        // 지갑 누적계산
        tradeService.updateWalletAfterTrade(buyOrder, this.ticker, tradedSize, totalTradedPrice);
        tradeService.updateWalletAfterTrade(sellOrder, this.ticker, tradedSize, totalTradedPrice);

        // 마지막 체결내역 임시 보관(웹소켓 전송용)
        lastTradeEventDto.setSize(tradedSize);
        lastTradeEventDto.setPrice(tradedPrice);
        lastTradeEventDto.setTimestamp(LocalDateTime.now());

        // 체결내역 저장
        tradeService.insertNewTrade(this.ticker, buyOrder, sellOrder, tradedSize, tradedPrice);

        // TODO : 호가 조회를 위한 Order Service 메서드 호출
    }

    private static TradeUnitPriceAndSize getTradeUnitPriceAndSize(BuyOrder buyOrder, SellOrder sellOrder) {
        double tradedPrice;
        double tradedSize;
        if (isMarketOrder(buyOrder)) { // 시장가매수-지정가매도
            tradedPrice = sellOrder.getPrice();
            if (buyOrder.getRemainingDeposit() >= tradedPrice * sellOrder.getRemainingSize()) { // 매수 잔여예수금이 매도 잔여량보다 크거나 같은 경우 (매수 부분체결 or 완전체결, 매도 완전체결)
                tradedSize = sellOrder.getRemainingSize();
            } else {
                tradedSize = buyOrder.getRemainingDeposit() / tradedPrice;
            }
        } else if (isMarketOrder(sellOrder)) { // 시장가매도-지정가매수
            tradedPrice = buyOrder.getPrice();
            tradedSize = Math.min(sellOrder.getRemainingSize(), buyOrder.getRemainingSize());
        } else { // 지정가매수-지정가매도
            tradedPrice = getTradedUnitPrice(buyOrder, sellOrder);
            tradedSize = Math.min(buyOrder.getRemainingSize(), sellOrder.getRemainingSize());
        }
        TradeUnitPriceAndSize tradeUnitPriceAndSize = new TradeUnitPriceAndSize(tradedSize, tradedPrice);
        return tradeUnitPriceAndSize;
    }

    private record TradeUnitPriceAndSize(double tradedSize, double tradedPrice) {
    }

    private static Boolean isMarketOrder(Order order) {
        return order.getIsMarketOrder();
    }

    private static Boolean isLimitOrder(Order order) {
        return !order.getIsMarketOrder();
    }

    private void writeTradingLog(BuyOrder buyOrder, SellOrder sellOrder) {
        logger.debug("[{}] 체결 확정!  종목: {}, ({}: {}가 {}로 {}만큼 매수주문), ({}: {}가 {}로 {}만큼 매도주문)",
                Thread.currentThread().threadId(),
                buyOrder.getTicker(),
                buyOrder.getId(),
                buyOrder.getUserId(),
                isMarketOrder(buyOrder) ? "시장가" : "지정가(" + buyOrder.getPrice() + "원)",
                buyOrder.getRemainingSize() == null ? buyOrder.getRemainingDeposit() : buyOrder.getRemainingSize(),
                sellOrder.getId(),
                sellOrder.getUserId(),
                isMarketOrder(sellOrder) ? "시장가" : "지정가(" + sellOrder.getPrice() + "원)",
                sellOrder.getRemainingSize());
    }

    private void removeCompletedBuyOrder(BuyOrder order) {
        boolean isOrderCompleted = (isMarketOrder(order) && order.getRemainingDeposit() < MINIMUM_ORDER_SIZE) ||
                (isLimitOrder(order) && order.getRemainingSize() < MINIMUM_ORDER_SIZE);

        if (isOrderCompleted) {
            PriorityBlockingQueue<? extends Order> orderQueue =
                    isMarketOrder(order) ? this.marketBuyOrderQueue : this.limitBuyOrderQueue;
            orderQueue.remove(order);
            tradeService.updateCompletedOrderStatus(order);
        }
    }

    private void removeCompletedSellOrder(SellOrder order) {
        boolean isOrderCompleted = order.getRemainingSize() < MINIMUM_ORDER_SIZE;

        if (isOrderCompleted) {
            PriorityBlockingQueue<? extends Order> orderQueue =
                    order.getIsMarketOrder() ? this.marketSellOrderQueue : this.limitSellOrderQueue;
            orderQueue.remove(order);
            tradeService.updateCompletedOrderStatus(order);
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

}