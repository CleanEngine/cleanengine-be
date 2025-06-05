package com.cleanengine.coin.user.info.application;

import com.cleanengine.coin.user.domain.Account;
import com.cleanengine.coin.user.info.infra.AccountRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

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

}
