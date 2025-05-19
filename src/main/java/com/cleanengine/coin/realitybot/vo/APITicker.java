package com.cleanengine.coin.realitybot.vo;

import lombok.Getter;

@Getter
public enum APITicker {
    TRUMP("trump"), BTC("btc");

    private final String name;
     APITicker(String name) {
        this.name = name;
    }

}
