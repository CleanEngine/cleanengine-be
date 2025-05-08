package com.cleanengine.coin.user.login.application;

import com.cleanengine.coin.user.domain.Account;
import com.cleanengine.coin.user.domain.OAuth;
import com.cleanengine.coin.user.domain.User;
import com.cleanengine.coin.user.login.infra.CustomOAuth2User;
import com.cleanengine.coin.user.login.infra.KakaoResponse;
import com.cleanengine.coin.user.login.infra.OAuth2Response;
import com.cleanengine.coin.user.login.infra.UserOAuthDetails;
import com.cleanengine.coin.user.info.infra.AccountRepository;
import com.cleanengine.coin.user.info.infra.OAuthRepository;
import com.cleanengine.coin.user.info.infra.UserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final OAuthRepository oAuthRepository;
    private final AccountRepository accountRepository;

    public CustomOAuth2UserService(UserRepository userRepository, OAuthRepository oAuthRepository, AccountRepository accountRepository) {
        this.userRepository = userRepository;
        this.oAuthRepository = oAuthRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        System.out.println("oAuth2User : " + oAuth2User);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        OAuth2Response oAuth2Response = new KakaoResponse(oAuth2User.getAttributes());
        /* 추후 OAuth 플랫폼 추가 시 이런 식으로 Response 분기처리
        if (registrationId.equals("kakao")) {
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

            Account newAccount = new Account();
            newAccount.setUserId(newUser.getId());
            newAccount.setCash((double) 50_000_000);
            accountRepository.save(newAccount);

            UserOAuthDetails userOAuthDetails = new UserOAuthDetails(newUser, newOAuth);

            return new CustomOAuth2User(userOAuthDetails);
        }
        else {
            OAuth existOAuth = oAuthRepository.findByProviderAndProviderUserId(provider, providerUserId);

            existOAuth.setEmail(email);
            existOAuth.setNickname(oAuth2Response.getName());
            // TODO : KAKAO Token 관련 정보 추가
            oAuthRepository.save(existOAuth);

            return new CustomOAuth2User(existData);
        }
    }

}
