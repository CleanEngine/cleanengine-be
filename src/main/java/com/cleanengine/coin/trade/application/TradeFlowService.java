package com.cleanengine.coin.trade.application;

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
            } catch (Exception e) {
                // TODO : 회복 필요
                log.error("Error processing trades for {}: {}", ticker, e.getMessage());
                continueProcessing = false;
            }
        }
    }

}
