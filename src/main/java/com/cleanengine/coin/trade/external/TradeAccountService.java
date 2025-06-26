package com.cleanengine.coin.trade.external;

import com.cleanengine.coin.trade.repository.trade.TradeAccountRepository;
import com.cleanengine.coin.user.domain.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class TradeAccountService {

    private final TradeAccountRepository tradeAccountRepository;

    public Optional<Account> findAccountByUserId(Integer userId) {
        return tradeAccountRepository.findByUserId(userId);
    }

    public Account save(Account account) {
        return tradeAccountRepository.save(account);
    }

}
