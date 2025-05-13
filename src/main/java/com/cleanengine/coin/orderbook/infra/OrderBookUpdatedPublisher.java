package com.cleanengine.coin.orderbook.infra;

import com.cleanengine.coin.orderbook.application.service.OrderBookUpdatedPort;
import com.cleanengine.coin.orderbook.domain.OrderPriceInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class OrderBookUpdatedPublisher implements OrderBookUpdatedPort {
    @Override
    public void sendOrderBooks(List<OrderPriceInfo> buyOrderBookList, List<OrderPriceInfo> sellOrderBookList) {
        log.info("sendOrderBooks");
    }
}
