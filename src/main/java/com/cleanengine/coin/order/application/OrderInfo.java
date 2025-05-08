package com.cleanengine.coin.order.application;

import com.cleanengine.coin.order.domain.OrderStatus;
import lombok.Builder;

import java.time.LocalDateTime;

public record OrderInfo(
        Long id,
        String ticker,
        OrderStatus state,
        Integer userId,
        String side,
        String orderType,
        Double LockedDeposit,
        Double orderSize,
        Double price,
        LocalDateTime createdAt
) {
    @Builder
    public OrderInfo {}
}
