package com.cleanengine.coin.realitybot.api;

public interface ExchangesAPIClient {
    String get(String ticker);
    String getExchangeName();
}
