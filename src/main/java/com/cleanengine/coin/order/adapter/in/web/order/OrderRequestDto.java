package com.cleanengine.coin.order.adapter.in.web.order;

import com.cleanengine.coin.order.adapter.in.web.order.validation.OrderType;
import com.cleanengine.coin.order.adapter.in.web.order.validation.Side;
import com.cleanengine.coin.order.application.dto.OrderCommand;
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

        public OrderCommand.CreateOrder toOrderCommand(Integer userId) {
            boolean isMarketOrder;
            boolean isBuyOrder;

            // TODO 나중에 검증 로직 Bean Validation + Custom Annotation으로 깔끔하게 정리
            isBuyOrder = side.equals("bid");
            isMarketOrder = orderType.equals("market");

            return new OrderCommand.CreateOrder(ticker, userId, isBuyOrder, isMarketOrder,
                    orderSize, price, false);
        }
    }
}

