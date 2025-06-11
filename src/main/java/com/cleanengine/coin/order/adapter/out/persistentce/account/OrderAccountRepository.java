package com.cleanengine.coin.order.adapter.out.persistentce.account;

import com.cleanengine.coin.user.domain.Account;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface OrderAccountRepository extends CrudRepository<Account, Integer> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
//    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")})
    Optional<Account> findByUserId(Integer userId);
}
