package com.cleanengine.coin.trade.external;

import com.cleanengine.coin.trade.repository.trade.TradeAccountRepository;
import com.cleanengine.coin.trade.repository.trade.TradeWalletRepository;
import com.cleanengine.coin.user.domain.Wallet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TradeWalletService {

    private final TradeWalletRepository tradeWalletRepository;
    private final TradeAccountRepository tradeAccountRepository;

    public Wallet save(Wallet wallet) {
        return tradeWalletRepository.save(wallet);
    }

    public Wallet findWalletByUserIdAndTicker(Integer userId, String ticker) {
        int accountId = tradeAccountRepository.findByUserId(userId).orElseThrow().getId();
        return tradeWalletRepository.findByAccountIdAndTicker(accountId, ticker)
                .orElseGet(() -> Wallet.of(ticker, accountId));
    }
}
