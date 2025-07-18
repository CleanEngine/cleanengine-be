package com.cleanengine.coin.user.info.presentation;

import com.cleanengine.coin.common.util.SecurityUtil;
import com.cleanengine.coin.order.application.OrderCancelService;
import com.cleanengine.coin.tool.annotation.WithCustomMockUser;
import com.cleanengine.coin.user.domain.Account;
import com.cleanengine.coin.user.info.application.AccountService;
import com.cleanengine.coin.user.info.application.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AccountService accountService;

    @MockitoBean
    private SecurityUtil securityUtil;

    @MockitoBean
    private OrderCancelService orderCancelService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @WithCustomMockUser
    @DisplayName("정상적으로 존재하는 사용자 정보를 통해 조회에 성공한다.")
    public void testRetrieveUserInfoSuccess() throws Exception {
        int userId = 1;
        String email = "test@test.com";
        String nickname = "test";
        String provider = "kakao";
        double cash = 1000.0;

        UserInfoDTO userInfoDTO = UserInfoDTO.of(userId, email, nickname, provider, cash, null, 0.0);
        when(userService.retrieveUserInfoByUserId(userId)).thenReturn(userInfoDTO);

        Account account = Account.of(userId, cash);
        when(accountService.retrieveAccountByUserId(userId)).thenReturn(account);
        when(securityUtil.getCurrentUserId()).thenReturn(Optional.of(userId));

        mockMvc.perform(get("/api/userinfo"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.isSuccess", is(true)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.cash", is((int) cash)));
    }

    @Test
    @DisplayName("인증되지 않은 사용자가 사용자 정보 요청 시 리디렉션 응답을 반환한다.")
    public void testRetrieveUserInfoUnauthorized() throws Exception {
        mockMvc.perform(get("/api/userinfo"))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection());
        verifyNoInteractions(userService, accountService);
    }

    @Test
    @WithCustomMockUser(id = 3)
    @DisplayName("성공적으로 사용자의 계정을 초기화한다.")
    public void testResetAccountSuccess() throws Exception {
        // given
        int userId = 3;
        when(securityUtil.getCurrentUserId()).thenReturn(Optional.of(userId));

        // when
        mockMvc.perform(post("/api/account/reset")
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.isSuccess", is(true)));

        // then
        verify(orderCancelService).cancelAllForReset(userId);
        verify(accountService).resetWithWallets(userId);
    }

    @Test
    @DisplayName("인증되지 않은 사용자가 계정 초기화 요청 시 리디렉션 응답을 반환한다.")
    public void testResetAccountUnauthorized() throws Exception {
        // given
        when(securityUtil.getCurrentUserId()).thenReturn(Optional.empty());

        // when, then
        mockMvc.perform(post("/api/account/reset")
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection());

        verifyNoInteractions(orderCancelService, accountService);
    }

    @Test
    @WithCustomMockUser(id = 3)
    @DisplayName("계정 초기화 중 에러 발생 시 실패 응답을 반환한다.")
    public void testResetAccountException() throws Exception {
        // given
        int userId = 3;

        when(securityUtil.getCurrentUserId()).thenReturn(Optional.of(userId));

        doThrow(new RuntimeException("Unexpected error")).when(orderCancelService).cancelAllForReset(userId);

        // when, then
        mockMvc.perform(post("/api/account/reset")
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.isSuccess", is(false)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.error.errorCode", is("A88")));

        verify(orderCancelService).cancelAllForReset(userId);
        verifyNoInteractions(accountService);
    }

}