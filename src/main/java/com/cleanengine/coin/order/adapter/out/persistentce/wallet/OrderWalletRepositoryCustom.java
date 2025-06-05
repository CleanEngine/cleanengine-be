package com.cleanengine.coin.order.adapter.out.persistentce.wallet;

import com.cleanengine.coin.user.domain.Wallet;

import java.util.Optional;

public interface OrderWalletRepositoryCustom {
    Optional<Wallet> findWalletBy(Integer userId, String ticker);
}
