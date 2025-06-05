package com.cleanengine.coin.user.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("계좌 단위테스트")
class AccountTest {

    @DisplayName("계좌의 예수금을 5000만큼 증가시킨다.")
    @Test
    void increaseCash() {
        // given
        Account account = Account.of(1, 1000.0);

        // when
        account.increaseCash(5000.0);

        // then
        assertEquals(6000.0, account.getCash());
    }

    @DisplayName("계좌의 예수금을 0만큼 증가시키면 예외가 발생한다.")
    @Test
    void increaseZeroCash() {
        // given
        Account account = Account.of(1, 1000.0);

        // when, then
        assertThatThrownBy(() -> account.increaseCash(0.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Increase amount must be greater than zero.");
    }

    @DisplayName("계좌의 예수금을 5000만큼 감소시킨다.")
    @Test
    void decreaseCash() {
        // given
        Account account = Account.of(1, 6000.0);

        // when, then
        assertEquals(1000.0, account.decreaseCash(5000.0).getCash());
    }

    @DisplayName("계좌의 예수금을 0만큼 감소시키면 예외가 발생한다.")
    @Test
    void decreaseZeroCash() {
        // given
        Account account = Account.of(1, 6000.0);

        // when, then
        assertThatThrownBy(() -> account.decreaseCash(0.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Decrease amount must be greater than zero.");
    }

    @DisplayName("계좌의 예수금보다 많은 금액을 감소시키면 예외가 발생한다.")
    @Test
    void decreaseCashUnderRemainingCash() {
        // given
        Account account = Account.of(1, 1000.0);

        // when, then
        assertThatThrownBy(() -> account.decreaseCash(5000.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot decrease cash. Available cash: 1000.0, requested: 5000.0");
    }

}