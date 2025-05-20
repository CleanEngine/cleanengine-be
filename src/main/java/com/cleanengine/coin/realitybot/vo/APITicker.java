package com.cleanengine.coin.realitybot.vo;

import lombok.Getter;

@Getter
public enum APITicker {
    TRUMP("TRUMP"), BTC("BTC");

    private final String name;
     APITicker(String name) {
        this.name = name;
    }

}
