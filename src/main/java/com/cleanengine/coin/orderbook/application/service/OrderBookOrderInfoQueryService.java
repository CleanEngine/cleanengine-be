package com.cleanengine.coin.orderbook.application.service;

import com.cleanengine.coin.order.application.dto.OrderInfo;
import com.cleanengine.coin.orderbook.infra.OrderBookBuyOrderQueryRepository;
import com.cleanengine.coin.orderbook.infra.OrderBookSellOrderQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderBookOrderInfoQueryService {

    private final OrderBookBuyOrderQueryRepository orderBookBuyOrderQueryRepository;
    private final OrderBookSellOrderQueryRepository orderBookSellOrderQueryRepository;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Optional<OrderInfo> getOrderInfo(long id, boolean isBuyOrder) {
        if(isBuyOrder) {
            return orderBookBuyOrderQueryRepository.getBuyOrderInfo(id).map(orderInfo -> orderInfo);
        }
        else {
            return orderBookSellOrderQueryRepository.getSellOrderInfo(id).map(orderInfo -> orderInfo);
        }
    }
}
