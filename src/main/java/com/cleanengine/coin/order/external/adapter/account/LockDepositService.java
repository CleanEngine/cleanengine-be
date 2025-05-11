package com.cleanengine.coin.order.external.adapter.account;

import com.cleanengine.coin.common.error.DomainValidationException;
import com.cleanengine.coin.user.domain.Account;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;

import java.util.List;

@Component
public class LockDepositService {
    public void lockDeposit(Account account, Double orderAmount){
        // TODO 원래라면 이 로직은 Account 내에 있어야 함. 각 엔티티가 자신의 invariant를 보장해야 하므로
        if(account.getCash() < orderAmount){
            throw new DomainValidationException("not enough cash",
                    List.of(new FieldError("account", "cash", "not enough cash")));
        }
        account.setCash(account.getCash() - orderAmount);
    }
}
