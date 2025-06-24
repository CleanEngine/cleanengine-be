package com.cleanengine.coin.user.info.infra;

import com.cleanengine.coin.user.domain.Wallet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;
import java.util.Optional;

//@NoRepositoryBean
public interface WalletRepository extends JpaRepository<Wallet, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Wallet> findByAccountIdAndTicker(Integer accountId, String ticker);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Wallet> findByAccountId(Integer accountId);
}
