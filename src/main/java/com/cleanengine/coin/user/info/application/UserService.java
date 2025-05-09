package com.cleanengine.coin.user.info.application;

import com.cleanengine.coin.user.info.presentation.UserInfoDTO;
import com.cleanengine.coin.user.info.infra.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserInfoDTO retrieveUserInfo(String provider, String providerUserId) {
        return userRepository.findUserInfoByProviderAndProviderId(provider, providerUserId);
    }

}
