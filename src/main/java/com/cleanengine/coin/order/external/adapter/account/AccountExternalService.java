package com.cleanengine.coin.order.external.adapter.account;

import com.cleanengine.coin.common.error.DomainValidationException;
import com.cleanengine.coin.order.application.port.AccountUpdatePort;
import com.cleanengine.coin.user.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.FieldError;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountExternalService implements AccountUpdatePort {
    private final AccountExternalRepository accountRepository;
    private final LockDepositService lockDepositService;

    // TODO 동시성 문제 고려해야
    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void lockDepositForBuyOrder(Integer userId, Double orderAmount) throws RuntimeException {
        if(orderAmount <= 0){
            throw new DomainValidationException("orderAmount must be greater than 0",
                    List.of(new FieldError("BuyOrder", "lockedDeposit", "orderAmount must be greater than 0")));
        }
        Account account = accountRepository
                .findByUserId(userId)
                .orElseThrow(()->
                        new DomainValidationException("Account not found",
                                List.of(new FieldError("account", "userId", "user might not exist"))));
        lockDepositService.lockDeposit(account, orderAmount);
    }
}
