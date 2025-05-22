package com.cleanengine.coin.user.info.application;

import com.cleanengine.coin.user.info.infra.AccountRepository;
import com.cleanengine.coin.user.info.infra.WalletRepository;
import com.cleanengine.coin.user.info.presentation.UserInfoDTO;
import com.cleanengine.coin.user.info.infra.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final WalletRepository walletRepository;

    public UserService(UserRepository userRepository, AccountRepository accountRepository, WalletRepository walletRepository) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.walletRepository = walletRepository;
    }

    public UserInfoDTO retrieveUserInfoByUserId(Integer userId) {
        return userRepository.retrieveUserInfoByUserId(userId);
    }

}
