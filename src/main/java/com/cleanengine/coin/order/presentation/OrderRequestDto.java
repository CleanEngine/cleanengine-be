package com.cleanengine.coin.order.presentation;

import com.cleanengine.coin.order.application.OrderCommand;
import com.cleanengine.coin.order.presentation.validation.OrderType;
import com.cleanengine.coin.order.presentation.validation.Side;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.time.LocalDateTime;

public final class OrderRequestDto {
    private OrderRequestDto(){}

    public record CreateOrderRequest(
            String ticker,
            @Side
            String side,
            @OrderType
            String orderType,
            Double orderSize,
            Double price
    ){
        @JsonCreator
        public CreateOrderRequest {}

        public OrderCommand.CreateOrder toOrderCommand(Integer userId, LocalDateTime createdAt) {
            boolean isMarketOrder;
            boolean isBuyOrder;

            // TODO 나중에 검증 로직 Bean Validation + Custom Annotation으로 깔끔하게 정리
            isBuyOrder = side.equals("bid");
            isMarketOrder = orderType.equals("market");

            return new OrderCommand.CreateOrder(ticker, userId, isBuyOrder, isMarketOrder,
                    orderSize, price, createdAt, false);
        }
    }
}

