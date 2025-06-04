package com.cleanengine.coin.user.info.application;

import com.cleanengine.coin.common.CommonValues;
import com.cleanengine.coin.user.domain.Account;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles({"dev", "it", "h2-mem"})
@DisplayName("계좌 서비스 - h2 통합테스트")
@SpringBootTest
class AccountServiceTest {

    @Autowired
    private AccountService accountService;

    @DisplayName("유저 ID와 예수금으로 신규 계좌를 생성한다.")
    @Test
    void createNewAccount() {
        // given
        int userId = 3;
        double cash = CommonValues.INITIAL_USER_CASH;

        // when
        Account account = accountService.createNewAccount(userId, cash);
        assertThat(account).isNotNull();

        Account retrievedAccount = accountService.retrieveAccountByUserId(userId);

        // then
        assertThat(retrievedAccount).isNotNull()
                .extracting(Account::getUserId, Account::getCash)
                .containsExactly(userId, cash);
    }

    @DisplayName("존재하지 않는 userId로 조회 시 null을 반환한다.")
    @Test
    void retrieveAccountByInvalidUserId() {
        // given, when
        Account account = accountService.retrieveAccountByUserId(1000);

        // then
        assertThat(account).isNull();
    }

}