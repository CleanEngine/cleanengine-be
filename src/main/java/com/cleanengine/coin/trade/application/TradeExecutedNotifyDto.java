package com.cleanengine.coin.trade.application;

import com.cleanengine.coin.trade.entity.Trade;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@JsonPropertyOrder({"ticker", "price", "size", "type", "tradedTime"})
public class TradeExecutedNotifyDto {

    private String ticker;

    private Double price;

    private Double size;

    private String type;

    private LocalDateTime tradedTime;

    @Builder
    private TradeExecutedNotifyDto(String ticker, Double price, Double size, String type, LocalDateTime tradedTime) {
        this.ticker = ticker;
        this.price = price;
        this.size = size;
        this.type = type;
        this.tradedTime = tradedTime;
    }

    public static TradeExecutedNotifyDto of(Trade trade, String type) {
        return TradeExecutedNotifyDto.builder()
                .ticker(trade.getTicker())
                .price(trade.getPrice())
                .size(trade.getSize())
                .type(type)
                .tradedTime(trade.getTradeTime())
                .build();
    }

}
