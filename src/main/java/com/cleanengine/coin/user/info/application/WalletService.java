package com.cleanengine.coin.user.info.application;

import com.cleanengine.coin.user.domain.Account;
import com.cleanengine.coin.user.domain.Wallet;
import com.cleanengine.coin.user.info.infra.WalletRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WalletService {

    private final WalletRepository walletRepository;
    private final AccountService accountService;

    public WalletService(WalletRepository walletRepository, AccountService accountService) {
        this.walletRepository = walletRepository;
        this.accountService = accountService;
    }

    public List<Wallet> retrieveWalletsByAccountId(Integer accountId) {
        return walletRepository.findByAccountId(accountId);
    }

    public Wallet save(Wallet wallet) {
        return walletRepository.save(wallet);
    }

    public Wallet findWalletByUserIdAndTicker(Integer userId, String ticker) {
        Account account = accountService.findAccountByUserId(userId).orElseThrow();
        return walletRepository.findByAccountIdAndTicker(account.getId(), ticker)
                .orElseGet(() -> createNewWallet(account.getId(), ticker));
    }

    public Wallet createNewWallet(Integer accountId, String ticker) {
        Wallet newWallet = new Wallet();
        newWallet.setAccountId(accountId);
        newWallet.setTicker(ticker);
        newWallet.setSize(0.0);
        newWallet.setBuyPrice(0.0);
        newWallet.setRoi(0.0);
        return newWallet;
    }

}
