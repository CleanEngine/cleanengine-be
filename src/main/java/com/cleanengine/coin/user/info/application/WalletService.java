package com.cleanengine.coin.user.info.application;

import com.cleanengine.coin.user.domain.Wallet;
import com.cleanengine.coin.user.info.infra.WalletRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WalletService {

    private final WalletRepository walletRepository;

    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    public List<Wallet> retrieveWalletsByAccountId(Integer accountId) {
        return walletRepository.findByAccountId(accountId);
    }

}
