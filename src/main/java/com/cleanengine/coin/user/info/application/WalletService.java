package com.cleanengine.coin.user.info.application;

import com.cleanengine.coin.user.domain.Wallet;
import com.cleanengine.coin.user.info.infra.AccountRepository;
import com.cleanengine.coin.user.info.infra.WalletRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WalletService {

    private final WalletRepository walletRepository;
    private final AccountRepository accountRepository;

    public WalletService(WalletRepository walletRepository, AccountRepository accountRepository) {
        this.walletRepository = walletRepository;
        this.accountRepository = accountRepository;
    }

    public List<Wallet> findByAccountId(Integer accountId) {
        return walletRepository.findByAccountId(accountId);
    }

    public Wallet save(Wallet wallet) {
        return walletRepository.save(wallet);
    }

    public Wallet findWalletByUserIdAndTicker(Integer userId, String ticker) {
        int accountId = accountRepository.findByUserId(userId).orElseThrow().getId();
        return walletRepository.findByAccountIdAndTicker(accountId, ticker)
                .orElseGet(() -> Wallet.generateEmptyWallet(ticker, accountId));
    }

}
