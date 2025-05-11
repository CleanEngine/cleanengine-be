package com.cleanengine.coin.order.external.adapter.wallet;

import com.cleanengine.coin.user.Wallet;

import java.util.Optional;

public interface WalletExternalRepositoryCustom {
    Optional<Wallet> findWalletBy(Integer userId, String ticker);
}
