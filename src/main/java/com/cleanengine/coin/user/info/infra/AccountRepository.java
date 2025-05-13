package com.cleanengine.coin.user.info.infra;

import com.cleanengine.coin.user.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Integer> {
}
