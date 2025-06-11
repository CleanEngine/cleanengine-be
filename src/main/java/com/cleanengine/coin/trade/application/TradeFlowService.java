package com.cleanengine.coin.trade.application;

import com.cleanengine.coin.order.domain.BuyOrder;
import com.cleanengine.coin.order.domain.Order;
import com.cleanengine.coin.order.domain.spi.WaitingOrders;
import com.cleanengine.coin.order.domain.spi.WaitingOrdersManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
public class TradeFlowService {

    private final TradeMatcher tradeMatcher;
    private final TradeExecutor tradeExecutor;
    private final WaitingOrdersManager waitingOrdersManager;

    public void execMatchAndTrade(String ticker) {
        WaitingOrders waitingOrders = waitingOrdersManager.getWaitingOrders(ticker);
        // TODO : peek() 해온 Order 객체들을 lock -> 체결 도중 취소 방지
        Optional<TradePair<Order, Order>> tradePair = tradeMatcher.matchOrders(waitingOrders);
        boolean continueProcessing = tradePair.isPresent();

        while (continueProcessing) {
            try {
                tradeExecutor.executeTrade(waitingOrders, tradePair.get(), ticker);
                tradePair = tradeMatcher.matchOrders(waitingOrders);
                continueProcessing = tradePair.isPresent();
            } catch (TradeZeroOrderException e) {
                Order order = e.getOrder();
                waitingOrdersManager.getWaitingOrders(order.getTicker()).removeOrder(order);
                log.warn("{} - {} 주문 {} 이 주문 수량 0 이므로 제거되었음.", order.getTicker(), order instanceof BuyOrder ? "매수" : "매도", order.getId());
                tradePair = tradeMatcher.matchOrders(waitingOrders);
                continueProcessing = tradePair.isPresent();
            } catch (Exception e) {
                log.error("{} - 체결 에러 발생: {}", ticker, e.getMessage());
                continueProcessing = false;
            }
        }
    }

}
