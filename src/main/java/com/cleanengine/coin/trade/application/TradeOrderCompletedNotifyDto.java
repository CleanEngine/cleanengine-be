package com.cleanengine.coin.trade.application;

import com.cleanengine.coin.order.domain.BuyOrder;
import com.cleanengine.coin.order.domain.Order;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@JsonPropertyOrder({"ticker", "size", "type"})
public class TradeOrderCompletedNotifyDto {

    private String ticker;

    private Double size;

    private String type;

    @Builder
    private TradeOrderCompletedNotifyDto(String ticker, Double price, Double size, String type, LocalDateTime tradedTime) {
        this.ticker = ticker;
        this.size = size;
        this.type = type;
    }

    public static TradeOrderCompletedNotifyDto of(Order order) {
        String orderType = order instanceof BuyOrder ? "ASK" : "BID";
        return TradeOrderCompletedNotifyDto.builder()
                .ticker(order.getTicker())
                .size(order.getOrderSize())
                .type(orderType)
                .build();
    }

}
