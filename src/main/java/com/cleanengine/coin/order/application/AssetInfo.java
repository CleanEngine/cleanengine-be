package com.cleanengine.coin.order.application;

import com.cleanengine.coin.order.domain.Asset;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"ticker", "name"})
public record AssetInfo(
        String ticker,
        String name
){
    public static AssetInfo from(Asset asset){
        return new AssetInfo(asset.getTicker(), asset.getName());
    }
}
