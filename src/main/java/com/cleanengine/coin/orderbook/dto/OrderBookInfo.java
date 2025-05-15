package com.cleanengine.coin.orderbook.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

@JsonPropertyOrder({"ticker", "buyOrderBookUnits", "sellOrderBookUnits"})
public record OrderBookInfo(
        String ticker,
        @JsonProperty("buyOrderBookUnits")List<OrderBookUnitInfo> buyOrderBookUnitInfos,
        @JsonProperty("sellOrderBookUnits")List<OrderBookUnitInfo> sellOrderBookUnitInfos
){}
