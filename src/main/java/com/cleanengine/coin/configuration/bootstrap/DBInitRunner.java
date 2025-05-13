package com.cleanengine.coin.configuration.bootstrap;

import com.cleanengine.coin.order.external.adapter.wallet.WalletExternalRepository;
import com.cleanengine.coin.user.domain.Account;
import com.cleanengine.coin.user.domain.User;
import com.cleanengine.coin.user.domain.Wallet;
import com.cleanengine.coin.user.info.infra.AccountRepository;
import com.cleanengine.coin.user.info.infra.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class DBInitRunner implements CommandLineRunner {
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final WalletExternalRepository walletExternalRepository;


    @Transactional
    @Override
    public void run(String... args) throws Exception {
        initBuyBotData();
        initSellBotData();
    }

    @Transactional
    protected void initSellBotData(){
        User user = new User();
        userRepository.save(user);

        Account account = new Account();
        account.setUserId(user.getId());
        account.setCash(0.0);
        accountRepository.save(account);

        Wallet wallet = new Wallet();
        wallet.setTicker("BTC");
        wallet.setAccountId(account.getId());
        wallet.setSize(500_000_000.0);

        Wallet wallet2 = new Wallet();
        wallet2.setTicker("TRUMP");
        wallet2.setAccountId(account.getId());
        wallet2.setSize(500_000_000.0);
        walletExternalRepository.saveAll(List.of(wallet, wallet2));
    }

    @Transactional
    protected void initBuyBotData() {
        User user = new User();
        userRepository.save(user);

        Account account = new Account();
        account.setUserId(user.getId());
        account.setCash(500_000_000.0);
        accountRepository.save(account);

        Wallet wallet = new Wallet();
        wallet.setTicker("BTC");
        wallet.setAccountId(account.getId());
        wallet.setSize(0.0);

        Wallet wallet2 = new Wallet();
        wallet2.setTicker("TRUMP");
        wallet2.setAccountId(account.getId());
        wallet2.setSize(0.0);
        walletExternalRepository.saveAll(List.of(wallet, wallet2));
    }
}
