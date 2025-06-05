package com.cleanengine.coin.order.adapter.out.persistentce.wallet;

import com.cleanengine.coin.user.domain.Wallet;
import org.springframework.data.repository.CrudRepository;

public interface OrderWalletRepository extends CrudRepository<Wallet, Long>, OrderWalletRepositoryCustom {
}
