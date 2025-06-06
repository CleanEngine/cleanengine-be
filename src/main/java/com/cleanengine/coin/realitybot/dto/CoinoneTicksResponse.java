package com.cleanengine.coin.realitybot.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CoinoneTicksResponse {
    private String result;
    private String error_code;
    private long server_time;
    private String quote_currency;
    private String target_currency;
    private List<Trades> trades;

    @Getter
    @Setter
    public static class Trades {
        private String id;
        private long timestamp;
        private String price;
        private String qty;
        private boolean is_seller_maker;
    }
}
