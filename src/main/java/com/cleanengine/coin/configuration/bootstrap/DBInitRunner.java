package com.cleanengine.coin.configuration.bootstrap;

import com.cleanengine.coin.order.adapter.out.persistentce.asset.AssetRepository;
import com.cleanengine.coin.order.domain.Asset;
import com.cleanengine.coin.user.domain.Account;
import com.cleanengine.coin.user.domain.User;
import com.cleanengine.coin.user.domain.Wallet;
import com.cleanengine.coin.user.info.infra.AccountRepository;
import com.cleanengine.coin.user.info.infra.UserRepository;
import com.cleanengine.coin.user.info.infra.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
@Profile({"(dev & !it & !mariadb-local) | h2-mem"})
@Order(1)
@RequiredArgsConstructor
public class DBInitRunner implements CommandLineRunner {
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final AssetRepository assetRepository;

    @Transactional
    @Override
    public void run(String... args) throws Exception {
        if(userRepository.count() == 0){
            initSellBotData();
            initBuyBotData();
        }
        if(assetRepository.count() == 0){
            initAssetData();
        }
    }

    @Transactional
    protected void initSellBotData(){
        User user = new User();
        userRepository.save(user);

        Account account = new Account();
        account.setUserId(user.getId());
        account.setCash(0.0);
        accountRepository.save(account);

        List<Wallet> wallets = new ArrayList<>();

        String[] tickers = {
                "BTC", "TRUMP", "ETH", "DOGE", "USDT", "PEPE", "XRP", "SOL", "SUI", "WLD",
                "LM", "ROA", "SNT", "PCI", "KAIA", "VIRTUAL", "EGG"
        };
        for (String ticker : tickers) {
            Wallet wallet = new Wallet();
            wallet.setTicker(ticker);
            wallet.setAccountId(account.getId());
            if ("PEPE".equals(ticker)) {
                wallet.setSize(10_000_000_000.0); // 10억으로 설정
            } else {
                wallet.setSize(1_000_000_000.0); // 다른 토큰은 5억
            }
            wallets.add(wallet);
        }

        walletRepository.saveAll(wallets);
    }

    @Transactional
    protected void initBuyBotData() {
        User user = new User();
        userRepository.save(user);

        Account account = new Account();
        account.setUserId(user.getId());
        account.setCash(50_000_000_000.0);
        accountRepository.save(account);

        List<Wallet> wallets = new ArrayList<>();

        String[] tickers = {
                "BTC", "TRUMP", "ETH", "DOGE", "USDT", "PEPE", "XRP", "SOL", "SUI", "WLD",
                 "LM", "ROA", "SNT", "PCI", "KAIA", "VIRTUAL", "EGG"
        };
        for (String ticker : tickers) {
            Wallet wallet = new Wallet();
            wallet.setTicker(ticker);
            wallet.setAccountId(account.getId());
            wallet.setSize(0.0);
            wallets.add(wallet);
        }
        walletRepository.saveAll(wallets);
    }

    @Transactional
    protected void initAssetData() {
        assetRepository.saveAll(List.of(
                new Asset("BTC", "비트코인"),
                new Asset("TRUMP", "오피셜 트럼프"),
                new Asset("ETH", "이더리움"),
                new Asset("DOGE", "도지코인"),
                new Asset("USDT", "테더"),
                new Asset("PEPE", "페페"),
                new Asset("XRP", "리플"),
                new Asset("SOL", "솔라나"),
                new Asset("SUI", "수이"),
                new Asset("WLD", "월드코인"),
                new Asset("LM", "레저메타"),
                new Asset("ROA", "로아코어"),
                new Asset("SNT", "스테이터스네트워크토큰"),
                new Asset("PCI", "페이코인"),
                new Asset("KAIA", "카이아"),
                new Asset("VIRTUAL", "버추얼 프로토콜"),
                new Asset("EGG", "네스트리")
        ));
    }
}
