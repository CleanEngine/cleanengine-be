package com.cleanengine.coin.user.info.infra.main;

import com.cleanengine.coin.user.info.infra.AccountRepository;
import org.springframework.context.annotation.Primary;

@Primary
public interface MainAccountRepository extends AccountRepository {
}
