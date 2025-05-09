package com.cleanengine.coin.order.presentation;

import com.cleanengine.coin.order.application.OrderInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.time.LocalDateTime;

public final class OrderResponseDto {
    private OrderResponseDto() {}

    @JsonPropertyOrder({"orderId", "createdAt"})
    public record CreateOrder(
            @JsonProperty("orderId")
            Long id,
            @JsonProperty("createdAt")
            LocalDateTime createdAt
    ) {
        public static CreateOrder from(OrderInfo orderInfo) {
            return new CreateOrder(orderInfo.id(), orderInfo.createdAt());
        }
    }
}
