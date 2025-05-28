package com.cleanengine.coin.order.application.dto;

import com.cleanengine.coin.order.domain.Asset;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.nio.charset.StandardCharsets;

@JsonPropertyOrder({"ticker", "name"})
public record AssetInfo(
        String ticker,
        String name,
        String svgIconBase64
){
    public static AssetInfo from(Asset asset){
        byte[] iconBytes = asset.getIcon();

        String iconStr = null;

        if(iconBytes != null){
            iconStr = new String(iconBytes, StandardCharsets.UTF_8);
        }

        return new AssetInfo(asset.getTicker(), asset.getName(), iconStr);
    }
}
