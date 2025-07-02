package com.cleanengine.coin.user.info.presentation;

import com.cleanengine.coin.configuration.SecurityEndpoints;
import com.cleanengine.coin.user.domain.Account;
import com.cleanengine.coin.user.info.application.AccountService;
import com.cleanengine.coin.user.info.application.UserService;
import com.cleanengine.coin.user.info.application.WalletService;
import com.cleanengine.coin.user.login.application.CustomOAuth2UserService;
import com.cleanengine.coin.user.login.application.CustomSuccessHandler;
import com.cleanengine.coin.user.login.application.JWTUtil;
import com.cleanengine.coin.user.login.infra.CustomOAuth2User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Collection;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AccountService accountService;

    @MockitoBean
    private WalletService walletService;

    @MockitoBean
    private JWTUtil jwtUtil;

    @MockitoBean
    private CustomOAuth2UserService customOAuth2UserService;

    @MockitoBean
    private CustomSuccessHandler customSuccessHandler;

    @MockitoBean
    private SecurityEndpoints.EndpointConfig endpointConfig;

    @Mock
    private CustomOAuth2User customOAuth2User;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("정상적으로 존재하는 사용자 정보를 통해 조회에 성공한다.")
    public void testRetrieveUserInfoSuccess() throws Exception {
        int userId = 1;
        String email = "test@test.com";
        String nickname = "test";
        String provider = "kakao";
        double cash = 1000.0;

        when(customOAuth2User.getUserId()).thenReturn(userId);
        when(customOAuth2User.getAttributes()).thenReturn(null);
        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        when(customOAuth2User.getAuthorities()).thenAnswer((Answer<Collection<? extends GrantedAuthority>>) invocation -> authorities)
        ;

        Authentication authenticationToken = new UsernamePasswordAuthenticationToken(
                customOAuth2User, null, authorities
        );

        UserInfoDTO userInfoDTO = UserInfoDTO.of(userId, email, nickname, provider, cash, null, 0.0);
        when(userService.retrieveUserInfoByUserId(userId)).thenReturn(userInfoDTO);

        Account account = Account.of(userId, cash);
        when(accountService.retrieveAccountByUserId(userId)).thenReturn(account);

        mockMvc.perform(get("/api/userinfo")
                        .with(authentication(authenticationToken)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.isSuccess", is(true)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.cash", is((int) cash)));
    }

    @Test
    @DisplayName("인증되지 않은 사용자가 private api 접근 시 리디렉션 응답을 반환한다.")
    public void testRetrieveUserInfoUnauthorized() throws Exception {
        mockMvc.perform(get("/api/userinfo"))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection());
        verifyNoInteractions(userService, accountService, walletService);
    }

}