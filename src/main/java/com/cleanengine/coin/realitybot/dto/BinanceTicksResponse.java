package com.cleanengine.coin.realitybot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
public class BinanceTicksResponse {
    private long id;
    private double price;
    private double qty;
    private String quoteQty;
    private String time;
    @JsonProperty("isBuyerMaker")
    private boolean buyerMaker;
    @JsonProperty("isBestMatch")
    private boolean BestMatch;
}
