package com.cleanengine.coin.user.info.infra;

import com.cleanengine.coin.user.domain.Account;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface AccountQueryRepository extends CrudRepository<Account, Integer> {
    Optional<Account> findByUserId(Integer userId);
}
