package com.cleanengine.coin.user.login.application;

import com.cleanengine.coin.common.CommonValues;
import com.cleanengine.coin.user.domain.OAuth;
import com.cleanengine.coin.user.domain.User;
import com.cleanengine.coin.user.info.application.AccountService;
import com.cleanengine.coin.user.login.infra.CustomOAuth2User;
import com.cleanengine.coin.user.login.infra.UserOAuthDetails;
import com.cleanengine.coin.user.info.infra.OAuthRepository;
import com.cleanengine.coin.user.info.infra.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@DisplayName("OAuth2 유저 서비스 단위테스트")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CustomOAuth2UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private OAuthRepository oAuthRepository;
    @Mock
    private AccountService accountService;
    @Mock
    private OAuth2UserRequest userRequest;
    @Mock
    private OAuth2User oAuth2UserFromSuper;
    @Mock
    private ClientRegistration clientRegistration;

    private CustomOAuth2UserService customOAuth2UserService;

    private final String provider = "kakao";

    private final String providerId = "12345";

    @BeforeEach
    void setUp() {
        // DefaultOAuth2UserService.loadUser만 mocking하기 위해 spy 사용
        customOAuth2UserService = Mockito.spy(new CustomOAuth2UserService(userRepository, oAuthRepository, accountService));

        Map<String, Object> profile = Map.of("nickname", "Test User");
        Map<String, Object> kakaoAccount = Map.of(
                "email", "test@example.com",
                "profile", profile
        );
        Map<String, Object> attributes = Map.of(
                "id", providerId,
                "kakao_account", kakaoAccount
        );
        when(oAuth2UserFromSuper.getAttributes()).thenReturn(attributes);

        when(userRequest.getClientRegistration()).thenReturn(clientRegistration);
        when(clientRegistration.getRegistrationId()).thenReturn(provider);

        try {
            doReturn(oAuth2UserFromSuper)
                .when(customOAuth2UserService)
                .doSuperLoadMethod(userRequest);
        } catch (OAuth2AuthenticationException e) {
            fail("doSuperLoadMethod mocking 실패", e);
        }
    }

    @DisplayName("신규 유저로 인증 시 User와 OAuth를 새로 생성한다.")
    @Test
    void whenNewUser_thenCreateUserAndOAuth() {
        // Given
        int userId = 3;
        when(userRepository.findUserByOAuthProviderAndProviderId(provider, providerId)).thenReturn(null);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(userId);
            return user;
        });

        // When
        OAuth2User result = customOAuth2UserService.loadUser(userRequest);

        // Then
        assertNotNull(result);
        verify(userRepository).save(any(User.class));
        verify(oAuthRepository).save(any(OAuth.class));
        verify(accountService).createNewAccount(eq(userId), eq(CommonValues.INITIAL_USER_CASH));

        assertInstanceOf(CustomOAuth2User.class, result);
        assertEquals("Test User", result.getName());
    }

    @DisplayName("기존 유저로 인증 시 이메일과 닉네임을 변경한다.")
    @Test
    void loadUser_WhenExistingUser_ShouldUpdateOAuth() {
        // Given
        User existingUser = User.of(3, LocalDateTime.now());

        OAuth existingOAuth = new OAuth();
        existingOAuth.setUserId(3);
        existingOAuth.setProvider(provider);
        existingOAuth.setProviderUserId(providerId);
        existingOAuth.setEmail("old@example.com");
        existingOAuth.setNickname("Old User");

        UserOAuthDetails userOAuthDetails = UserOAuthDetails.of(existingUser, existingOAuth);

        when(userRepository.findUserByOAuthProviderAndProviderId(provider, providerId)).thenReturn(userOAuthDetails);
        when(oAuthRepository.findByProviderAndProviderUserId(provider, providerId)).thenReturn(existingOAuth);
        when(oAuthRepository.save(any(OAuth.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        OAuth2User result = customOAuth2UserService.loadUser(userRequest);

        // Then
        assertNotNull(result);
        verify(oAuthRepository).save(existingOAuth);
        assertEquals("test@example.com", existingOAuth.getEmail());
        assertEquals("Test User", existingOAuth.getNickname());

        verify(userRepository, never()).save(any(User.class));
        verify(accountService, never()).createNewAccount(anyInt(), anyLong());

        assertInstanceOf(CustomOAuth2User.class, result);
        assertEquals("Test User", result.getName());
    }
}