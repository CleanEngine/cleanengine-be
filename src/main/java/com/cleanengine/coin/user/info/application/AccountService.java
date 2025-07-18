package com.cleanengine.coin.user.info.application;

import com.cleanengine.coin.common.CommonValues;
import com.cleanengine.coin.user.domain.Account;
import com.cleanengine.coin.user.domain.Wallet;
import com.cleanengine.coin.user.info.infra.AccountRepository;
import com.cleanengine.coin.user.info.infra.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class AccountService {

    private final AccountRepository accountRepository;

    private final WalletRepository walletRepository;

    @Value("${account.initial-cash.buy-bot}")
    private double initialCashBuyBot;

    @Value("${account.initial-cash.sell-bot}")
    private double initialCashSellBot;

    @Value("${account.initial-cash.user}")
    private double initialCashUser;

    @Value("${account.initial-wallet.sell-bot}")
    private double initialWalletSellBot;

    @Transactional
    public Account retrieveAccountByUserId(Integer userId) {
        return accountRepository.findByUserId(userId).orElse(null);
    }

    public Account save(Account account) {
        return accountRepository.save(account);
    }

    public Account createNewAccount(Integer userId, double cash) {
        Account account = Account.of(userId, cash);
        return accountRepository.save(account);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void resetBot(String ticker) {
        List<Account> accountsOfBot = accountRepository.findAccountsOfBot();
        accountsOfBot.forEach(this::resetCash);
        accountRepository.saveAll(accountsOfBot);

        List<Wallet> walletsOfBot = walletRepository.findWalletsOfBotByTicker(ticker);
        walletsOfBot.forEach(this::resetWallet);
        walletRepository.saveAll(walletsOfBot);
    }

    public int increaseAccountCash(int userId, double amount) {
        return accountRepository.increaseAccountCash(userId, amount);
    }

    @Transactional
    public void resetWithWallets(Integer userId) {
        Account account = accountRepository.findByUserId(userId).orElseThrow();
        this.resetCash(account);
        accountRepository.save(account);

        List<Wallet> wallets = walletRepository.findByAccountId(account.getId());
        wallets.forEach(this::resetWallet);
        walletRepository.saveAll(wallets);
    }

    private void resetCash(Account account) {
        double initialCash;

        if (account.getUserId() == CommonValues.BUY_ORDER_BOT_ID)
            initialCash = initialCashBuyBot;
        else if (account.getUserId() == CommonValues.SELL_ORDER_BOT_ID)
            initialCash = initialCashSellBot;
        else
            initialCash = initialCashUser;
        account.resetCash(initialCash);
    }

    private void resetWallet(Wallet wallet) {
        double initialSize;

        if (wallet.getAccountId() == CommonValues.SELL_ORDER_BOT_ID) {
            initialSize = initialWalletSellBot;
        } else {
            initialSize = 0.0;
        }
        wallet.reset(initialSize);
    }

}
