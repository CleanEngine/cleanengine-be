package com.cleanengine.coin.order.adapter.out.persistentce.account;

import com.cleanengine.coin.user.domain.Account;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface AccountExternalRepository extends CrudRepository<Account, Integer> {
    // TODO null 대처 해야
    Optional<Account> findByUserId(Integer userId);
}
