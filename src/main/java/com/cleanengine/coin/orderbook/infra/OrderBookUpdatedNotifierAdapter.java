package com.cleanengine.coin.orderbook.infra;

import com.cleanengine.coin.orderbook.application.service.OrderBookUpdatedNotifierPort;
import com.cleanengine.coin.orderbook.dto.OrderBookInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderBookUpdatedNotifierAdapter implements OrderBookUpdatedNotifierPort {
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void sendOrderBooks(OrderBookInfo orderBookInfo) {
        messagingTemplate.convertAndSend("/topic/orderbook/" + orderBookInfo.ticker(), orderBookInfo);
    }
}
