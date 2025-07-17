package com.cleanengine.coin.user.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Wallet entity 단위테스트")
class WalletTest {

    private static final int ACCOUNT_ID = 123;

    private static final String TICKER = "BTC";

    private static final double INITIAL_BUY_PRICE = 50000.0;

    private static final double INITIAL_SIZE = 100.0;

    @DisplayName("유효한 값을 통해 수량을 정상적으로 감소시킨다.")
    @Test
    void testDecreaseSize_WithValidOrderSize_ShouldReduceSize() {
        // given
        Wallet wallet = Wallet.of(TICKER, ACCOUNT_ID, INITIAL_BUY_PRICE, INITIAL_SIZE);
        double orderSize = 25.0;

        // when
        wallet.decreaseSize(orderSize);

        // then
        assertEquals(75.0, wallet.getSize());
    }

    @DisplayName("0의 수량을 감소시키면 예외가 발생한다.")
    @Test
    void testDecreaseSize_WithZeroOrderSize_ShouldThrowException() {
        // given
        Wallet wallet = Wallet.of(TICKER, ACCOUNT_ID, INITIAL_BUY_PRICE, INITIAL_SIZE);
        double orderSize = 0.0;

        // when, then
        assertThatThrownBy(() -> wallet.decreaseSize(orderSize))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("orderSize must be greater than zero.");
    }

    @DisplayName("음수의 수량을 감소시키면 예외가 발생한다.")
    @Test
    void testDecreaseSize_WithNegativeOrderSize_ShouldThrowException() {
        // given
        Wallet wallet = Wallet.of(TICKER, ACCOUNT_ID, INITIAL_BUY_PRICE, INITIAL_SIZE);
        double orderSize = -10.0;

        // when, then
        assertThatThrownBy(() -> wallet.decreaseSize(orderSize))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("orderSize must be greater than zero.");
    }

    @DisplayName("보유 수량보다 많은 수량을 감소시키면 예외가 발생한다.")
    @Test
    void testDecreaseSize_WhenOrderSizeExceedsWalletSize_ShouldThrowException() {
        // given
        Wallet wallet = Wallet.of(TICKER, ACCOUNT_ID, INITIAL_BUY_PRICE, INITIAL_SIZE);
        double orderSize = 200.0;

        // when, then
        assertThatThrownBy(() -> wallet.decreaseSize(orderSize))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot decrease size. Available size: 100.0, requested: 200.0");
    }

    @DisplayName("보유 수량과 같은 수량을 감소시키면 잔고는 0이 되어야 한다.")
    @Test
    void testDecreaseSize_WhenOrderSizeEqualsCurrentSize_ShouldSetSizeToZero() {
        // given
        Wallet wallet = Wallet.of(TICKER, ACCOUNT_ID, INITIAL_BUY_PRICE, INITIAL_SIZE);
        double orderSize = 100.0;

        // when
        wallet.decreaseSize(orderSize);

        // then
        assertEquals(0.0, wallet.getSize());
    }

    @DisplayName("수량을 여러 번 감소시킬 때도 정상적으로 동작한다.")
    @Test
    void testDecreaseSize_RepeatedDecreases_ShouldWorkCorrectly() {
        // given
        Wallet wallet = Wallet.of(TICKER, ACCOUNT_ID, INITIAL_BUY_PRICE, INITIAL_SIZE);

        // when
        wallet.decreaseSize(30.0);
        wallet.decreaseSize(20.0);

        // then
        assertEquals(50.0, wallet.getSize());
    }

    @DisplayName("예외 발생 후에는 잔액이 변하지 않아야 한다.")
    @Test
    void testDecreaseSize_Exception_ShouldNotChangeSize() {
        // given
        Wallet wallet = Wallet.of(TICKER, ACCOUNT_ID, INITIAL_BUY_PRICE, INITIAL_SIZE);

        try {
            // when
            wallet.decreaseSize(INITIAL_SIZE + 10);
        } catch (IllegalArgumentException e) {
            // then
            assertEquals(INITIAL_SIZE, wallet.getSize());
        }
    }

    @DisplayName("유효한 값을 통해 수량을 정상적으로 증가시킨다.")
    @Test
    void testIncreaseSize_WithValidOrderSize_ShouldIncreaseSize() {
        // given
        Wallet wallet = Wallet.of(TICKER, ACCOUNT_ID, INITIAL_BUY_PRICE, INITIAL_SIZE);
        double orderSize = 20.0;

        // when
        wallet.increaseSize(orderSize);

        // then
        assertEquals(120.0, wallet.getSize());
    }

    @DisplayName("0의 수량을 증가시키면 예외가 발생한다.")
    @Test
    void testIncreaseSize_WithZeroOrderSize_ShouldThrowException() {
        // given
        Wallet wallet = Wallet.of(TICKER, ACCOUNT_ID, INITIAL_BUY_PRICE, INITIAL_SIZE);
        double orderSize = 0.0;

        // when, then
        assertThatThrownBy(() -> wallet.increaseSize(orderSize))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("orderSize must be greater than zero.");
    }

    @DisplayName("음수의 수량을 증가시키면 예외가 발생한다.")
    @Test
    void testIncreaseSize_WithNegativeOrderSize_ShouldThrowException() {
        // given
        Wallet wallet = Wallet.of(TICKER, ACCOUNT_ID, INITIAL_BUY_PRICE, INITIAL_SIZE);
        double orderSize = -15.0;

        // when, then
        assertThatThrownBy(() -> wallet.increaseSize(orderSize))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("orderSize must be greater than zero.");
    }

    @DisplayName("수량을 여러 번 증가시킬 때도 정상적으로 동작한다.")
    @Test
    void testIncreaseSize_RepeatedIncreases_ShouldWorkCorrectly() {
        // given
        Wallet wallet = Wallet.of(TICKER, ACCOUNT_ID, INITIAL_BUY_PRICE, INITIAL_SIZE);

        // when
        wallet.increaseSize(10.0);
        wallet.increaseSize(15.0);

        // then
        assertEquals(125.0, wallet.getSize());
    }

    @DisplayName("예외 발생 후에는 잔액이 변하지 않아야 한다.")
    @Test
    void testIncreaseSize_Exception_ShouldNotChangeSize() {
        // given
        Wallet wallet = Wallet.of(TICKER, ACCOUNT_ID, INITIAL_BUY_PRICE, INITIAL_SIZE);

        try {
            // when
            wallet.increaseSize(-5.0);
        } catch (IllegalArgumentException e) {
            // then
            assertEquals(INITIAL_SIZE, wallet.getSize());
        }
    }

    @DisplayName("reset 메서드를 호출하면 size, buyPrice, roi가 초기값으로 설정되어야 한다.")
    @Test
    void testReset_ShouldSetAllFieldsToDefaultValues() {
        // given
        Wallet wallet = Wallet.of(TICKER, ACCOUNT_ID, INITIAL_BUY_PRICE, INITIAL_SIZE);
        wallet.setBuyPrice(50000.0);
        wallet.setRoi(10.0);

        // when
        wallet.reset();

        // then
        assertEquals(0.0, wallet.getSize(), "Size should be reset to 0.0");
        assertEquals(0.0, wallet.getBuyPrice(), "Buy price should be reset to 0.0");
        assertEquals(0.0, wallet.getRoi(), "ROI should be reset to 0.0");
    }

    @DisplayName("reset 메서드는 ticker와 accountId 값을 변경하지 않아야 한다.")
    @Test
    void testReset_ShouldNotAffectTickerAndAccountId() {
        // given
        Wallet wallet = Wallet.of(TICKER, ACCOUNT_ID, INITIAL_BUY_PRICE, INITIAL_SIZE);

        // when
        wallet.reset();

        // then
        assertEquals(TICKER, wallet.getTicker(), "Ticker should remain unchanged");
        assertEquals(ACCOUNT_ID, wallet.getAccountId(), "Account ID should remain unchanged");
    }

    @DisplayName("초기 상태에서 수량과 평단을 정상적으로 갱신한다.")
    @Test
    void testUpdateAfterPurchase_ShouldUpdateSizeAndBuyPriceCorrectly() {
        // given
        Wallet wallet = Wallet.of(TICKER, ACCOUNT_ID);
        double price = 60000.0;
        double addedSize = 100.0;

        // when
        wallet.updateAfterPurchase(price, addedSize);

        // then
        assertEquals(100.0, wallet.getSize(), "The size should be increased correctly.");
        assertEquals(60000.0, wallet.getBuyPrice(), "The buy price should match the trade price as it's the first trade.");
    }

    @DisplayName("updateAfterPurchase 메서드에 0의 price를 전달하면 예외가 발생한다.")
    @Test
    void testUpdateAfterPurchase_WithZeroPrice_ShouldThrowException() {
        // given
        Wallet wallet = Wallet.of(TICKER, ACCOUNT_ID, INITIAL_BUY_PRICE, INITIAL_SIZE);
        double price = 0.0;
        double addedSize = 10.0;

        // when, then
        assertThatThrownBy(() -> wallet.updateAfterPurchase(price, addedSize))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("price must be greater than zero.");
    }

    @DisplayName("updateAfterPurchase 메서드에 0의 addedSize를 전달하면 예외가 발생한다.")
    @Test
    void testUpdateAfterPurchase_WithZeroAddedSize_ShouldThrowException() {
        // given
        Wallet wallet = Wallet.of(TICKER, ACCOUNT_ID, INITIAL_BUY_PRICE, INITIAL_SIZE);
        double price = 60000.0;
        double addedSize = 0.0;

        // when, then
        assertThatThrownBy(() -> wallet.updateAfterPurchase(price, addedSize))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("addedSize must be greater than zero.");
    }

    @DisplayName("updateAfterPurchase 메서드는 평단을 올바르게 계산한다.")
    @Test
    void testUpdateAfterPurchase_ShouldUpdateBuyPriceWithWeightedAverage() {
        // given
        Wallet wallet = Wallet.of(TICKER, ACCOUNT_ID, INITIAL_BUY_PRICE, INITIAL_SIZE);
        double price = 60000.0;
        double addedSize = 100.0;

        // when
        wallet.updateAfterPurchase(price, addedSize);

        // then
        assertEquals(55000.0, wallet.getBuyPrice(), "The buy price should be updated with a weighted average.");
        assertEquals(200.0, wallet.getSize(), "The size should be incremented correctly.");
    }

}
