package com.cleanengine.coin.order.application.port.out;

public interface AccountUpdatePort {
    void lockDepositForBuyOrder(Integer userId, Double orderAmount) throws RuntimeException;
}
