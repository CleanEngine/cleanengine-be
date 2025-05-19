package com.cleanengine.coin.order.application.port;

public interface AccountUpdatePort {
    void lockDepositForBuyOrder(Integer userId, Double orderAmount) throws RuntimeException;
}
