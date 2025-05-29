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
@SpringBootTest
class AccountServiceTest {

    @Autowired
    private AccountService accountService;

    @DisplayName("유저 ID와 예수금으로 신규 계좌를 생성한다.")
    @Test
    void test() {
        // given
        int userId = 3;
        double cash = CommonValues.INITIAL_USER_CASH;

        // when
        accountService.createNewAccount(userId, cash);
        Account account = accountService.retrieveAccountByUserId(userId);

        // then
        assertThat(account).isNotNull()
                .extracting(Account::getUserId, Account::getCash)
                .containsExactly(userId, cash);
    }

}