package com.cleanengine.coin.orderbook.presentation;

import com.cleanengine.coin.orderbook.application.service.ReadOrderBookUsecase;
import com.cleanengine.coin.orderbook.dto.OrderBookInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class OrderBookWebSocketController{
    private final ReadOrderBookUsecase readOrderBookUsecase;

    @SubscribeMapping("/orderbook/{ticker}")
    public OrderBookInfo handleOrderBookSubscription(@DestinationVariable String ticker) {
        return readOrderBookUsecase.getOrderBook(ticker);
    }
}
