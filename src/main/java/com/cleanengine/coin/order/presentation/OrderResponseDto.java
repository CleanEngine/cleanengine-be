package com.cleanengine.coin.order.presentation;

import com.cleanengine.coin.order.application.OrderInfo;
import com.cleanengine.coin.order.domain.OrderStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.time.LocalDateTime;

public final class OrderResponseDto {
    private OrderResponseDto() {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({"orderId", "ticker", "state", "userId", "side", "orderType",
            "lockedDeposit", "orderSize", "price", "createdAt",})
    public record CreateOrder(
            @JsonProperty("orderId") Long id,
            @JsonProperty("ticker") String ticker,
            @JsonProperty("state") OrderStatus state,
            @JsonProperty("userId") Integer userId,
            @JsonProperty("side") String side,
            @JsonProperty("orderType") String orderType,
            @JsonProperty("lockedDeposit") Double lockedDeposit,
            @JsonProperty("orderSize") Double orderSize,
            @JsonProperty("price") Double price,
            @JsonProperty("createdAt") @JsonFormat(shape = JsonFormat.Shape.STRING) LocalDateTime createdAt
    ) {
        public static CreateOrder from(OrderInfo<?> orderInfo) {
            if(orderInfo instanceof OrderInfo.BuyOrderInfo){
                return from((OrderInfo.BuyOrderInfo) orderInfo);
            }
            else{
                return from((OrderInfo.SellOrderInfo) orderInfo);
            }
        }

        private static CreateOrder from(OrderInfo.BuyOrderInfo buyOrderInfo){
            return new CreateOrder(buyOrderInfo.getId(), buyOrderInfo.getTicker(), buyOrderInfo.getState(),
                    buyOrderInfo.getUserId(), "bid", getOrderType(buyOrderInfo.getIsMarketOrder()),
                    buyOrderInfo.getLockedDeposit(), buyOrderInfo.getOrderSize(), buyOrderInfo.getPrice(), buyOrderInfo.getCreatedAt());
        }

        private static CreateOrder from(OrderInfo.SellOrderInfo sellOrderInfo){
            return new CreateOrder(sellOrderInfo.getId(), sellOrderInfo.getTicker(), sellOrderInfo.getState(),
                    sellOrderInfo.getUserId(), "ask", getOrderType(sellOrderInfo.getIsMarketOrder()),
                    null, sellOrderInfo.getOrderSize(), sellOrderInfo.getPrice(), sellOrderInfo.getCreatedAt());
        }

        private static String getOrderType(boolean isMarketOrder){
            return isMarketOrder ? "market" : "limit";
        }
    }
}
