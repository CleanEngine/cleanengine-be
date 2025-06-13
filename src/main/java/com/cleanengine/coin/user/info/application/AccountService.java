package com.cleanengine.coin.user.info.application;

import com.cleanengine.coin.user.domain.Account;
import com.cleanengine.coin.user.domain.Wallet;
import com.cleanengine.coin.user.info.infra.AccountRepository;
import com.cleanengine.coin.user.info.infra.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.cleanengine.coin.common.CommonValues.BUY_ORDER_BOT_ID;
import static com.cleanengine.coin.common.CommonValues.SELL_ORDER_BOT_ID;

@RequiredArgsConstructor
@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final WalletRepository walletRepository;

    @Transactional
    public Account retrieveAccountByUserId(Integer userId) {
        return accountRepository.findByUserId(userId).orElse(null);
    }

    public Optional<Account> findAccountByUserId(Integer userId) {
        return accountRepository.findByUserId(userId);
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
        Account sellBotAccount = accountRepository.findByUserId(SELL_ORDER_BOT_ID).orElseThrow();
        sellBotAccount.setCash(0.0);
        Account buyBotAccount = accountRepository.findByUserId(BUY_ORDER_BOT_ID).orElseThrow();
        buyBotAccount.setCash(500_000_000.0);
        accountRepository.save(sellBotAccount);
        accountRepository.save(buyBotAccount);

        Wallet wallet = walletRepository.findByAccountIdAndTicker(SELL_ORDER_BOT_ID, ticker).orElseThrow();
        wallet.setSize(500_000_000.0);
        Wallet wallet2 = walletRepository.findByAccountIdAndTicker(BUY_ORDER_BOT_ID, ticker).orElseThrow();
        wallet2.setSize(0.0);
        walletRepository.save(wallet);
        walletRepository.save(wallet2);
    }
}
