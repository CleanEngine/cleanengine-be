package com.cleanengine.coin.order.adapter.out.persistentce.account;

import com.cleanengine.coin.user.domain.Account;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface OrderAccountRepository extends CrudRepository<Account, Integer> {
    Optional<Account> findByUserId(Integer userId);
}
