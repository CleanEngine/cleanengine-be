package com.cleanengine.coin.realitybot.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CoinbaseTicksResponse {
    private List<TradeDto> trades;

    private String best_bid;
    private String best_ask;

    @Getter
    @Setter
    public static class TradeDto{
        private String trade_id;
        private String product_id;
        private String price;
        private String size;
        private Timestamp time; //Timestamp -> string으로 전화
        private String side; //ask
        private String bid;
        private String ask;
        private String exchange;
    }
}
