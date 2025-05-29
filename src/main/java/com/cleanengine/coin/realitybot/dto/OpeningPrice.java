package com.cleanengine.coin.realitybot.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OpeningPrice {
    private String market;
    private double opening_price;
    private double trade_price;

    @Override
    public String toString() {
        return "OpeningPrice{" +
                "market='" + market + '\'' +
                ", OpeningPrice=" + opening_price +
                ", tradePrice=" + trade_price +
                '}';
    }
}
