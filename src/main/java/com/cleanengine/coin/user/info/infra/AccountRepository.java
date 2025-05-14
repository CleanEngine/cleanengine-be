package com.cleanengine.coin.user.info.infra;

import com.cleanengine.coin.user.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Integer> {

    Optional<Account> findByUserId(Integer userId);

}
