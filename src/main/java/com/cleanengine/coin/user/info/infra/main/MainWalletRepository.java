package com.cleanengine.coin.user.info.infra.main;

import com.cleanengine.coin.user.info.infra.WalletRepository;
import org.springframework.context.annotation.Primary;

@Primary
public interface MainWalletRepository extends WalletRepository {
}
