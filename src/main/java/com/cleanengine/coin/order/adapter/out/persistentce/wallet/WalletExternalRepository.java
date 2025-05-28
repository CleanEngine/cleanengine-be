package com.cleanengine.coin.order.adapter.out.persistentce.wallet;

import com.cleanengine.coin.user.domain.Wallet;
import org.springframework.data.repository.CrudRepository;

public interface WalletExternalRepository extends CrudRepository<Wallet, Long>, WalletExternalRepositoryCustom {
}
