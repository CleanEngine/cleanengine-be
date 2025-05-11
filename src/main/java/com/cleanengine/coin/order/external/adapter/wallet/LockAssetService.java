package com.cleanengine.coin.order.external.adapter.wallet;

import com.cleanengine.coin.common.error.DomainValidationException;
import com.cleanengine.coin.user.Wallet;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;

import java.util.List;

@Component
public class LockAssetService {
    public void lockAsset(Wallet wallet, Double orderSize) {
        if(wallet.getSize() < orderSize){
            throw new DomainValidationException("not enough asset",
                    List.of(new FieldError("wallet", "size", "not enough asset")));
        }
        wallet.setSize(wallet.getSize() - orderSize);
    }
}
