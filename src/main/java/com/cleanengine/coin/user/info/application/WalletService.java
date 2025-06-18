package com.cleanengine.coin.user.info.application;

import com.cleanengine.coin.order.adapter.out.persistentce.asset.AssetRepository;
import com.cleanengine.coin.user.domain.Wallet;
import com.cleanengine.coin.user.info.infra.AccountRepository;
import com.cleanengine.coin.user.info.infra.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WalletService {

    private final WalletRepository walletRepository;
    private final AccountRepository accountRepository;
    private final AssetRepository assetRepository;

    public WalletService(WalletRepository walletRepository, AccountRepository accountRepository, AssetRepository assetRepository) {
        this.walletRepository = walletRepository;
        this.accountRepository = accountRepository;
        this.assetRepository = assetRepository;
    }

    @Transactional
    public List<Wallet> findByAccountId(Integer accountId) {
        return walletRepository.findByAccountId(accountId);
    }

    public Wallet save(Wallet wallet) {
        return walletRepository.save(wallet);
    }

    public List<Wallet> saveAll(List<Wallet> wallets) {
        return walletRepository.saveAll(wallets);
    }

    public Wallet findWalletByUserIdAndTicker(Integer userId, String ticker) {
        return walletRepository.findByUserIdAndTicker(userId, ticker)
                .orElseGet(() -> Wallet.of(ticker, accountRepository.findByUserId(userId).orElseThrow().getId()));
    }

    public List<Object[]> findWalletsByUserIdsAndTicker(List<Integer> userIds, String ticker) {
        return walletRepository.findAllByUserIdsAndTicker(userIds, ticker);
    }

    public void createNewWallets(Integer accountId) {
        assetRepository.findAll()
                .stream()
                .map(asset -> Wallet.of(asset.getTicker(), accountId))
                .forEach(walletRepository::save);
    }

}
