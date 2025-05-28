package com.cleanengine.coin.order.application.port.out;

public interface WalletUpdatePort {
    void lockAssetForSellOrder(Integer userId, String ticker, Double orderSize) throws RuntimeException;
}
