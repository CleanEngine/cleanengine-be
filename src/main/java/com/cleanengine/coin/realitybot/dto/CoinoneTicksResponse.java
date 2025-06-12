package com.cleanengine.coin.realitybot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    private List<Trades> transactions;

    @Getter
    @Setter
    public static class Trades {
        private String id;
        private long timestamp;
        private String price;
        private String qty;
        @JsonProperty("is_seller_maker")
        private boolean isSellerMaker;
    }
}
