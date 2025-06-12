package com.cleanengine.coin.user.login.application;

import com.cleanengine.coin.common.CommonValues;
import com.cleanengine.coin.user.domain.Account;
import com.cleanengine.coin.user.domain.OAuth;
import com.cleanengine.coin.user.domain.User;
import com.cleanengine.coin.user.info.application.AccountService;
import com.cleanengine.coin.user.info.application.WalletService;
import com.cleanengine.coin.user.login.infra.CustomOAuth2User;
import com.cleanengine.coin.user.login.infra.KakaoResponse;
import com.cleanengine.coin.user.login.infra.OAuth2Response;
import com.cleanengine.coin.user.login.infra.UserOAuthDetails;
import com.cleanengine.coin.user.info.infra.OAuthRepository;
import com.cleanengine.coin.user.info.infra.UserRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final OAuthRepository oAuthRepository;
    private final AccountService accountService;
    private final WalletService walletService;

    public CustomOAuth2UserService(UserRepository userRepository, OAuthRepository oAuthRepository, AccountService accountService, WalletService walletService) {
        this.userRepository = userRepository;
        this.oAuthRepository = oAuthRepository;
        this.accountService = accountService;
        this.walletService = walletService;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = doSuperLoadMethod(userRequest);

        OAuth2Response oAuth2Response = new KakaoResponse(oAuth2User.getAttributes());
        /* 추후 OAuth 플랫폼 추가 시 이런 식으로 Response 분기처리
        if (userRequest.getClientRegistration().getRegistrationId().equals("kakao")) {
            oAuth2Response = new KakaoResponse(oAuth2User.getAttributes());
        }
        else {
            return null;
        }
        */

        String provider = oAuth2Response.getProvider();
        String providerUserId = oAuth2Response.getProviderId();
        String email = oAuth2Response.getEmail();
        String name = oAuth2Response.getName();

        UserOAuthDetails existData = userRepository.findUserByOAuthProviderAndProviderId(provider, providerUserId);

        if (existData == null) {
            return createNewUser(provider, providerUserId, email, name);
        }
        else {
            OAuth existOAuth = oAuthRepository.findByProviderAndProviderUserId(provider, providerUserId);

            existOAuth.setEmail(email);
            existOAuth.setNickname(name);
            oAuthRepository.save(existOAuth);

            existData.update(existOAuth);

            return CustomOAuth2User.of(existData);
        }
    }

    @NotNull
    protected CustomOAuth2User createNewUser(String provider, String providerUserId, String email, String name) {
        User newUser = new User();
        userRepository.save(newUser);

        OAuth newOAuth = new OAuth();
        newOAuth.setUserId(newUser.getId());
        newOAuth.setProvider(provider);
        newOAuth.setProviderUserId(providerUserId);
        newOAuth.setEmail(email);
        newOAuth.setNickname(name);
        // TODO : KAKAO Token 관련 정보 추가
        oAuthRepository.save(newOAuth);
        Account newAccount = accountService.createNewAccount(newUser.getId(), CommonValues.INITIAL_USER_CASH);
        walletService.createNewWallets(newAccount.getId());

        UserOAuthDetails newUserOAuthDetails = UserOAuthDetails.of(newUser, newOAuth);
        return CustomOAuth2User.of(newUserOAuthDetails);
    }

    protected OAuth2User doSuperLoadMethod(OAuth2UserRequest userRequest) {
        return super.loadUser(userRequest);
    }

}
