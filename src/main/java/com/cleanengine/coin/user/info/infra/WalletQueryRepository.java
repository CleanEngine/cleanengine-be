package com.cleanengine.coin.user.info.infra;

import com.cleanengine.coin.user.domain.Wallet;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface WalletQueryRepository extends CrudRepository<Wallet, Long> {
    @Query("SELECT w FROM Wallet w JOIN Account a ON w.accountId = a.id WHERE a.userId = :userId AND w.ticker = :ticker")
    Optional<Wallet> findByUserIdAndTicker(Integer userId, String ticker);
}
