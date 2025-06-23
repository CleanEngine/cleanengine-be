package com.cleanengine.coin.user.info.infra;

import com.cleanengine.coin.user.domain.Account;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Account> findByUserId(Integer userId);

    @Modifying(flushAutomatically = true)
    @Query("UPDATE Account a SET a.cash = a.cash + :amount WHERE a.userId = :userId")
    int increaseAccountCash(int userId, double amount);

}
