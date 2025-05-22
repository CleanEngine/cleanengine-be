package com.cleanengine.coin.user.info.infra;

import com.cleanengine.coin.user.domain.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
    Optional<Wallet> findByAccountIdAndTicker(Integer accountId, String ticker);

    List<Wallet> findByAccountId(Integer accountId);
}
