package com.cleanengine.coin.order.external.adapter.wallet;

import com.cleanengine.coin.common.error.DomainValidationException;
import com.cleanengine.coin.order.application.port.WalletUpdatePort;
import com.cleanengine.coin.user.domain.Wallet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.FieldError;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WalletExternalService implements WalletUpdatePort {
    private final WalletExternalRepository walletRepository;
    private final LockAssetService lockAssetService;

    @Override
    public void lockAssetForSellOrder(Integer userId, String ticker, Double orderSize) throws RuntimeException {
        if(orderSize <= 0){
            throw new DomainValidationException("orderSize must be positive",
                    List.of(new FieldError("SellOrder", "orderSize", "orderSize must be positive")));
        }
        Wallet wallet = findWalletBy(userId, ticker);
        lockAssetService.lockAsset(wallet, orderSize);
    }

    private Wallet findWalletBy(Integer userId, String ticker) {
        Wallet wallet = walletRepository
                .findWalletBy(userId, ticker)
                .orElseThrow(()->
                        new DomainValidationException("Wallet not found",
                                List.of(new FieldError("wallet", "userId", "user might not exist"),
                                        new FieldError("wallet", "ticker", "ticker might be wrong"))));
        return wallet;
    }
}
