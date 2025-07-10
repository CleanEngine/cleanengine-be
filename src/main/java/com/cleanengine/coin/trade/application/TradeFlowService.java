package com.cleanengine.coin.trade.application;

import com.cleanengine.coin.order.domain.BuyOrder;
import com.cleanengine.coin.order.domain.Order;
import com.cleanengine.coin.order.domain.OrderStatus;
import com.cleanengine.coin.order.domain.spi.ActiveOrders;
import com.cleanengine.coin.order.domain.spi.ActiveOrdersManager;
import com.cleanengine.coin.order.domain.spi.WaitingOrders;
import com.cleanengine.coin.order.domain.spi.WaitingOrdersManager;
import com.cleanengine.coin.trade.entity.Trade;
import com.cleanengine.coin.trade.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@RequiredArgsConstructor
@Component
public class TradeFlowService {

    private final TradeMatcher tradeMatcher;
    private final TradeExecutor tradeExecutor;
    private final WaitingOrdersManager waitingOrdersManager;
    private final TradeRepository tradeRepository;
    private final ActiveOrdersManager activeOrdersManager;

    private CountDownLatch testLatch; // 테스트용 후크

    @Profile("trade-load-test")
    public void setTestLatch(CountDownLatch latch) {
        this.testLatch = latch;
    }

    public void execMatchAndTrade(String ticker) {
        WaitingOrders waitingOrders = waitingOrdersManager.getWaitingOrders(ticker);
        ActiveOrders activeOrders = activeOrdersManager.getActiveOrders(ticker);

        // TODO : peek() 해온 Order 객체들을 lock -> 체결 도중 취소 방지
        Optional<TradePair<Order, Order>> tradePair = tradeMatcher.matchOrders(waitingOrders);
        boolean continueProcessing = tradePair.isPresent();
        List<Trade> tradesToSave = new ArrayList<>();
        List<ReentrantLock> locks = new ArrayList<>();

        while (continueProcessing) {
            try {
                TradePair<Order, Order> pair = tradePair.get();

                locks = lockPair(activeOrders, pair);
                if (!canContinueExecution(pair)) {
                    unlockLocks(locks);
                    tradePair = tradeMatcher.matchOrders(waitingOrders);
                    continueProcessing = tradePair.isPresent();
                    continue;
                }
                Trade trade = tradeExecutor.executeTrade(waitingOrders, pair, ticker);
                unlockLocks(locks);

                tradesToSave.add(trade);
                if (tradesToSave.size() > 10000) {
                    tradeRepository.saveAll(tradesToSave);
                    tradesToSave.clear();
                }

                tradePair = tradeMatcher.matchOrders(waitingOrders);
                continueProcessing = tradePair.isPresent();
            } catch (TradeZeroOrderException e) {
                Order order = e.getOrder();
                waitingOrdersManager.getWaitingOrders(order.getTicker()).removeOrder(order);
                log.warn("{} - {} 주문 {} 이 주문 수량 0 이므로 제거되었음.", order.getTicker(), order instanceof BuyOrder ? "매수" : "매도", order.getId());
                unlockLocks(locks);
                tradePair = tradeMatcher.matchOrders(waitingOrders);
                continueProcessing = tradePair.isPresent();
            }
            catch (Exception e) {
                log.error("{} - 체결 에러 발생: {}", ticker, e.getMessage());
                continueProcessing = false;
                unlockLocks(locks);
            }
        }

        if (!tradesToSave.isEmpty()) {
            tradeRepository.saveAll(tradesToSave);
            tradesToSave.clear();
        }

        if (testLatch != null) {
            testLatch.countDown();
        }
    }

    private boolean canContinueExecution(TradePair<Order, Order> pair) {
        return pair.getBuyOrder().getState() != OrderStatus.CANCELED && pair.getSellOrder().getState() != OrderStatus.CANCELED;
    }

    private List<ReentrantLock> lockPair(ActiveOrders activeOrders, TradePair<Order, Order> pair) {
        List<ReentrantLock> locks = new ArrayList<>();
        locks.add(activeOrders.lockOrder(pair.getBuyOrder().getId()));
        try{
            locks.add(activeOrders.lockOrder(pair.getSellOrder().getId()));
        }
        catch(Exception e) {
            activeOrders.unlockOrder(pair.getBuyOrder().getId());
            throw e;
        }
        return locks;
    }

    private void unlockLocks(List<ReentrantLock> locks) {
        for (ReentrantLock lock : locks) {
            if(lock.isHeldByCurrentThread()) lock.unlock();
        }
    }

}
