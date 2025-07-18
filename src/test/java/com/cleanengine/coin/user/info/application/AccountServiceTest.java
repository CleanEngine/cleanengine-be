package com.cleanengine.coin.user.info.application;

import com.cleanengine.coin.user.domain.Account;
import com.cleanengine.coin.user.domain.Wallet;
import com.cleanengine.coin.user.info.infra.AccountRepository;
import com.cleanengine.coin.user.info.infra.WalletRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Optional;

import static com.cleanengine.coin.common.CommonValues.BUY_ORDER_BOT_ID;
import static com.cleanengine.coin.common.CommonValues.SELL_ORDER_BOT_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("AccountService 단위테스트")
@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private AccountService accountService;

    @Value("${account.initial-cash.buy-bot}")
    private double initialCashBuyBot;

    @Value("${account.initial-cash.sell-bot}")
    private double initialCashSellBot;

    @Test
    @DisplayName("봇 자산을 초기화한다.")
    void resetBot_successfullyResetsBots() {
        String ticker = "BTC";
        // given
        Account sellBotAccount = Account.of(SELL_ORDER_BOT_ID, 123456.0);
        Account buyBotAccount = Account.of(BUY_ORDER_BOT_ID, 123456.0);
        when(accountRepository.findAccountsOfBot()).thenReturn(List.of(sellBotAccount, buyBotAccount));

        Wallet sellBotWallet = Wallet.of(ticker, SELL_ORDER_BOT_ID, 0.0, 100.0);
        Wallet buyBotWallet = Wallet.of(ticker, BUY_ORDER_BOT_ID, 0.0, 200.0);
        when(walletRepository.findWalletsOfBotByTicker(ticker)).thenReturn(List.of(sellBotWallet, buyBotWallet));

        // when
        accountService.resetBot(ticker);

        // then
        verify(accountRepository).saveAll(List.of(sellBotAccount, buyBotAccount));
        verify(walletRepository).saveAll(List.of(sellBotWallet, buyBotWallet));
        assertThat(sellBotAccount.getCash()).isEqualTo(initialCashSellBot);
        assertThat(buyBotAccount.getCash()).isEqualTo(initialCashBuyBot);
    }

    @Test
    @DisplayName("정상적으로 계좌와 지갑들을 초기화한다.")
    void resetWithWallets_successfullyResetsAccountAndWallets() {
        // given
        Integer accountId = 3;
        Account account = Account.of(accountId, 1000.0);
        List<Wallet> wallets = List.of(
                Wallet.of("BTC", accountId, 100000.0, 50.0),
                Wallet.of("ETH", accountId, 20000.0, 20.0)
        );

        when(accountRepository.findByUserId(accountId)).thenReturn(Optional.of(account));
        when(walletRepository.findByAccountId(account.getId())).thenReturn(wallets);

        // when
        accountService.resetWithWallets(accountId);

        // then
        verify(accountRepository).save(account);
        verify(walletRepository).saveAll(wallets);
    }

}