package com.cleanengine.coin.trade.application;

import com.cleanengine.coin.order.domain.Order;
import com.cleanengine.coin.order.domain.spi.WaitingOrders;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
public class TradeFlowService {

    private final TradeMatcher tradeMatcher;
    private final TradeExecutor tradeExecutor;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void execMatchAndTrade(String ticker) {
        WaitingOrders waitingOrders = tradeMatcher.getWaitingOrders(ticker);
        // TODO : peek() 해온 Order 객체들을 lock -> 체결 도중 취소 방지
        Optional<TradePair<Order, Order>> tradePair = tradeMatcher.matchOrders(waitingOrders);

        tradePair.ifPresent(orderOrderTradePair -> tradeExecutor.executeTrade(waitingOrders, orderOrderTradePair, ticker));
    }

}
