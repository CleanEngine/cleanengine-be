package com.cleanengine.coin.user.info.application;

import com.cleanengine.coin.user.domain.Account;
import com.cleanengine.coin.user.domain.Wallet;
import com.cleanengine.coin.user.info.infra.AccountRepository;
import com.cleanengine.coin.user.info.infra.WalletRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("지갑 서비스-Repository 통합테스트")
@ActiveProfiles({"dev", "it", "h2-mem"})
@Transactional
@SpringBootTest
class WalletServiceTest {

    @Autowired
    private WalletService walletService;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private AccountRepository accountRepository;

    private Account testAccount;

    @BeforeEach
    void setUp() {
        // given
        int userId = 3;
        testAccount = Account.of(userId, 0.0);
        accountRepository.findById(userId).ifPresent(accountRepository::delete);
        accountRepository.save(testAccount);
        walletRepository.deleteAll(walletRepository.findByAccountId(testAccount.getId()));

        Wallet testWallet = Wallet.of("BTC", testAccount.getId(), 0.0, 1000.0);
        walletRepository.save(testWallet);
    }

    @AfterEach
    void tearDown() {
        accountRepository.findById(testAccount.getUserId()).ifPresent(accountRepository::delete);
        walletRepository.deleteAll(walletRepository.findByAccountId(testAccount.getId()));
    }

    @DisplayName("계좌 ID로 조회 시 지갑이 정상 반환된다.")
    @Test
    void findByAccountId_thenReturnWallet() {
        // when
        List<Wallet> wallets = walletService.findByAccountId(testAccount.getId());

        // then
        assertThat(wallets).isNotEmpty();
        assertThat(wallets.getFirst().getTicker()).isEqualTo("BTC");
    }

    @DisplayName("지갑이 성공적으로 저장된다.")
    @Test
    void save_thenCreateNewWallet() {
        // when
        Wallet newWallet = Wallet.of("TRUMP", testAccount.getId(), 0.0, 5000.0);
        Wallet savedWallet = walletService.save(newWallet);

        // then
        assertThat(savedWallet.getId()).isNotNull();
        assertThat(savedWallet.getTicker()).isEqualTo("TRUMP");
        assertThat(savedWallet.getSize()).isEqualTo(5000.0);
    }

    @DisplayName("유저 ID, 티커로 존재하는 지갑 조회 시 정상적으로 반환된다.")
    @Test
    void findWalletByUserIdAndTicker_ExistingWallet_thenReturnWallet() {
        // when
        Wallet wallet = walletService.findWalletByUserIdAndTicker(testAccount.getUserId(), "BTC");

        // then
        assertThat(wallet).isNotNull();
        assertThat(wallet.getId()).isNotNull();
        assertThat(wallet.getTicker()).isEqualTo("BTC");
        assertThat(wallet.getSize()).isEqualTo(1000.0);
    }

    @DisplayName("유저 ID, 티커로 존재하지 않는 지갑 조회 시 빈 지갑이 새로 반환된다.")
    @Test
    void findWalletByUserIdAndTicker_NonExistingWallet_thenReturnEmptyWallet() {
        // when
        Wallet wallet = walletService.findWalletByUserIdAndTicker(testAccount.getUserId(), "TRUMP");

        // then
        assertThat(wallet).isNotNull();
        assertThat(wallet.getTicker()).isEqualTo("TRUMP");
        assertThat(wallet.getSize()).isEqualTo(0.0);
    }

}
