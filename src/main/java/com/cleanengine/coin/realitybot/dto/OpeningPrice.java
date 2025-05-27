package com.cleanengine.coin.realitybot.dto;

import com.google.gson.annotations.SerializedName;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OpeningPrice {
    String market;
    @SerializedName("opening_price")
    double openingPrice;
    @SerializedName("trade_price")
    double tradePrice;

    @Override
    public String toString() {
        return "OpeningPrice{" +
                "market='" + market + '\'' +
                ", OpeningPrice=" + openingPrice +
                ", tradePrice=" + tradePrice +
                '}';
    }
}
