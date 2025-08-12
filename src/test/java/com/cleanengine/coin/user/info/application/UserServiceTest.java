package com.cleanengine.coin.user.info.application;

import com.cleanengine.coin.order.application.AssetService;
import com.cleanengine.coin.user.domain.Account;
import com.cleanengine.coin.user.domain.OAuth;
import com.cleanengine.coin.user.domain.Wallet;
import com.cleanengine.coin.user.info.infra.AccountRepository;
import com.cleanengine.coin.user.info.infra.OAuthRepository;
import com.cleanengine.coin.user.info.infra.WalletRepository;
import com.cleanengine.coin.user.info.presentation.UserInfoDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@DisplayName("UserService 단위테스트")
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private OAuthRepository oAuthRepository;

    @Mock
    private AssetService assetService;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("유저 ID를 통해 유저 정보를 정상적으로 불러온다.")
    void shouldRetrieveUserInfoSuccessfully() {
        // given
        Integer userId = 3;
        Account account = Account.of(userId, 1000.0);
        OAuth oAuth = OAuth.of(userId, "kakao", "123", "test@test.com", "name");
        Wallet wallet = Wallet.of("BTC", 1, 50000.0, 2.0);
        when(accountRepository.findByUserId(userId)).thenReturn(Optional.of(account));
        when(oAuthRepository.findByUserId(userId)).thenReturn(Optional.of(oAuth));
        when(walletRepository.findByAccountId(account.getId())).thenReturn(List.of(wallet));
        when(assetService.getCurrentPrice("BTC")).thenReturn(55000.0);
        when(assetService.getAssetName("BTC")).thenReturn("비트코인");

        // when
        UserInfoDTO result = userService.retrieveUserInfoByUserId(userId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getEmail()).isEqualTo(oAuth.getEmail());
        assertThat(result.getNickname()).isEqualTo(oAuth.getNickname());
        assertThat(result.getProvider()).isEqualTo(oAuth.getProvider());
        assertThat(result.getCash()).isEqualTo(account.getCash());
        assertThat(result.getWallets().getFirst().getTicker()).isEqualTo(wallet.getTicker());
        assertThat(result.getTotalAssetAmount()).isEqualTo(1000.0 + (2.0 * 55000.0));
    }

    @Test
    @DisplayName("사용자 계좌가 없는 경우 예외가 발생한다.")
    void shouldThrowExceptionWhenAccountNotFound() {
        // given
        Integer userId = 3;
        when(accountRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> userService.retrieveUserInfoByUserId(userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("계좌를 찾을 수 없습니다. userId: " + userId);
    }

    @Test
    @DisplayName("OAuth 정보가 없는 경우 예외가 발생한다.")
    void shouldThrowExceptionWhenOAuthNotFound() {
        // given
        Integer userId = 3;
        Account account = Account.of(userId, 1000.0);
        when(accountRepository.findByUserId(userId)).thenReturn(Optional.of(account));
        when(oAuthRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> userService.retrieveUserInfoByUserId(userId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("OAuth 정보를 찾을 수 없습니다. userId: " + userId);
    }

}