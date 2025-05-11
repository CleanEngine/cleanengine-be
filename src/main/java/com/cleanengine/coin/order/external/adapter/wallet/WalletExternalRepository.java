package com.cleanengine.coin.order.external.adapter.wallet;

import com.cleanengine.coin.user.Wallet;
import org.springframework.data.repository.CrudRepository;

public interface WalletExternalRepository extends CrudRepository<Wallet, Long>, WalletExternalRepositoryCustom {
}
