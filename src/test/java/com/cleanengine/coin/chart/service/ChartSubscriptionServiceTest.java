package com.cleanengine.coin.chart.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChartSubscriptionService 단위 테스트")
class ChartSubscriptionServiceTest {

    @InjectMocks
    private ChartSubscriptionService service;

    private String testTicker1;
    private String testTicker2;
    private String testTicker3;

    @BeforeEach
    void setUp() {
        testTicker1 = "BTC";
        testTicker2 = "ETH";
        testTicker3 = "TRUMP";
    }

    // ===== 실시간 체결 내역 구독 테스트 =====
    @Test
    @DisplayName("실시간 체결 정보 구독을 정상적으로 추가한다")
    void subscribeRealTimeTradeRate_ValidTicker_AddsSubscription() {
        // when
        service.subscribeRealTimeTradeRate(testTicker1);

        // then
        assertThat(service.isSubscribedToRealTimeTradeRate(testTicker1)).isTrue();
        assertThat(service.getAllRealTimeTradeRateSubscribedTickers()).contains(testTicker1);
    }

    @Test
    @DisplayName("실시간 체결 정보 구독을 정상적으로 해지한다")
    void unsubscribeRealTimeTradeRate_SubscribedTicker_RemovesSubscription() {
        // given
        service.subscribeRealTimeTradeRate(testTicker1);
        assertThat(service.isSubscribedToRealTimeTradeRate(testTicker1)).isTrue();

        // when
        service.unsubscribeRealTimeTradeRate(testTicker1);

        // then
        assertThat(service.isSubscribedToRealTimeTradeRate(testTicker1)).isFalse();
        assertThat(service.getAllRealTimeTradeRateSubscribedTickers()).doesNotContain(testTicker1);
    }

    @Test
    @DisplayName("모든 실시간 체결 정보 구독 티커를 올바르게 반환한다")
    void getAllRealTimeTradeRateSubscribedTickers_MultipleSubscriptions_ReturnsAllTickers() {
        // given
        service.subscribeRealTimeTradeRate(testTicker1);
        service.subscribeRealTimeTradeRate(testTicker2);
        service.subscribeRealTimeTradeRate(testTicker3);

        // when
        Set<String> result = service.getAllRealTimeTradeRateSubscribedTickers();

        // then
        assertThat(result).hasSize(3);
        assertThat(result).containsExactlyInAnyOrder(testTicker1, testTicker2, testTicker3);
    }

    @Test
    @DisplayName("실시간 체결 정보 구독 상태를 정확하게 확인한다")
    void isSubscribedToRealTimeTradeRate_VariousStates_ReturnsCorrectStatus() {
        // given
        service.subscribeRealTimeTradeRate(testTicker1);

        // then
        assertThat(service.isSubscribedToRealTimeTradeRate(testTicker1)).isTrue();
        assertThat(service.isSubscribedToRealTimeTradeRate(testTicker2)).isFalse();
        assertThat(service.isSubscribedToRealTimeTradeRate("NONEXISTENT")).isFalse();
    }

    @Test
    @DisplayName("동일한 티커를 여러 번 구독해도 중복되지 않는다")
    void subscribeRealTimeTradeRate_DuplicateSubscription_NoDuplicates() {
        // when
        service.subscribeRealTimeTradeRate(testTicker1);
        service.subscribeRealTimeTradeRate(testTicker1);
        service.subscribeRealTimeTradeRate(testTicker1);

        // then
        Set<String> subscriptions = service.getAllRealTimeTradeRateSubscribedTickers();
        assertThat(subscriptions).hasSize(1);
        assertThat(subscriptions).contains(testTicker1);
    }

    // ===== 실시간 OHLC 구독 테스트 =====
    @Test
    @DisplayName("실시간 OHLC 구독을 정상적으로 추가한다")
    void subscribeRealTimeOhlc_ValidTicker_AddsSubscription() {
        // when
        service.subscribeRealTimeOhlc(testTicker1);

        // then
        assertThat(service.isSubscribedToRealTimeOhlc(testTicker1)).isTrue();
        assertThat(service.getAllRealTimeOhlcSubscribedTickers()).contains(testTicker1);
    }

    @Test
    @DisplayName("실시간 OHLC 구독을 정상적으로 해지한다")
    void unsubscribeRealTimeOhlc_SubscribedTicker_RemovesSubscription() {
        // given
        service.subscribeRealTimeOhlc(testTicker1);
        assertThat(service.isSubscribedToRealTimeOhlc(testTicker1)).isTrue();

        // when
        service.unsubscribeRealTimeOhlc(testTicker1);

        // then
        assertThat(service.isSubscribedToRealTimeOhlc(testTicker1)).isFalse();
        assertThat(service.getAllRealTimeOhlcSubscribedTickers()).doesNotContain(testTicker1);
    }

    @Test
    @DisplayName("모든 실시간 OHLC 구독 티커를 올바르게 반환한다")
    void getAllRealTimeOhlcSubscribedTickers_MultipleSubscriptions_ReturnsAllTickers() {
        // given
        service.subscribeRealTimeOhlc(testTicker1);
        service.subscribeRealTimeOhlc(testTicker2);

        // when
        Set<String> result = service.getAllRealTimeOhlcSubscribedTickers();

        // then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(testTicker1, testTicker2);
    }

    @Test
    @DisplayName("실시간 OHLC 구독 상태를 정확하게 확인한다")
    void isSubscribedToRealTimeOhlc_VariousStates_ReturnsCorrectStatus() {
        // given
        service.subscribeRealTimeOhlc(testTicker1);

        // then
        assertThat(service.isSubscribedToRealTimeOhlc(testTicker1)).isTrue();
        assertThat(service.isSubscribedToRealTimeOhlc(testTicker2)).isFalse();
    }

    @Test
    @DisplayName("동일한 티커를 여러 번 OHLC 구독해도 중복되지 않는다")
    void subscribeRealTimeOhlc_DuplicateSubscription_NoDuplicates() {
        // when
        service.subscribeRealTimeOhlc(testTicker1);
        service.subscribeRealTimeOhlc(testTicker1);

        // then
        Set<String> subscriptions = service.getAllRealTimeOhlcSubscribedTickers();
        assertThat(subscriptions).hasSize(1);
        assertThat(subscriptions).contains(testTicker1);
    }

    // ===== 전날 종가 변동률 구독 테스트 =====
    @Test
    @DisplayName("전날 종가 변동률 구독을 정상적으로 추가한다")
    void subscribePrevRate_ValidTicker_AddsSubscription() {
        // when
        service.subscribePrevRate(testTicker1);

        // then
        assertThat(service.isSubscribedToPrevRate(testTicker1)).isTrue();
        assertThat(service.getAllPrevRateSubscribedTickers(testTicker1)).contains(testTicker1);
    }

    @Test
    @DisplayName("전날 종가 변동률 구독을 정상적으로 해지한다")
    void unsubscribePrevRate_SubscribedTicker_RemovesSubscription() {
        // given
        service.subscribePrevRate(testTicker1);
        assertThat(service.isSubscribedToPrevRate(testTicker1)).isTrue();

        // when
        service.unsubscribePrevRate(testTicker1);

        // then
        assertThat(service.isSubscribedToPrevRate(testTicker1)).isFalse();
        assertThat(service.getAllPrevRateSubscribedTickers(testTicker1)).doesNotContain(testTicker1);
    }

    @Test
    @DisplayName("모든 전날 종가 변동률 구독 티커를 올바르게 반환한다")
    void getAllPrevRateSubscribedTickers_MultipleSubscriptions_ReturnsAllTickers() {
        // given
        service.subscribePrevRate(testTicker1);
        service.subscribePrevRate(testTicker2);

        // when
        Set<String> result = service.getAllPrevRateSubscribedTickers("irrelevant"); // 파라미터는 무시됨

        // then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(testTicker1, testTicker2);
    }

    @Test
    @DisplayName("전날 종가 변동률 구독 상태를 정확하게 확인한다")
    void isSubscribedToPrevRate_VariousStates_ReturnsCorrectStatus() {
        // given
        service.subscribePrevRate(testTicker1);

        // then
        assertThat(service.isSubscribedToPrevRate(testTicker1)).isTrue();
        assertThat(service.isSubscribedToPrevRate(testTicker2)).isFalse();
    }

    @Test
    @DisplayName("동일한 티커를 여러 번 전날 종가 구독해도 중복되지 않는다")
    void subscribePrevRate_DuplicateSubscription_NoDuplicates() {
        // when
        service.subscribePrevRate(testTicker1);
        service.subscribePrevRate(testTicker1);

        // then
        Set<String> subscriptions = service.getAllPrevRateSubscribedTickers(testTicker1);
        assertThat(subscriptions).hasSize(1);
        assertThat(subscriptions).contains(testTicker1);
    }

    // ===== 혼합 시나리오 테스트 =====
    @Test
    @DisplayName("서로 다른 구독 타입은 독립적으로 관리된다")
    void multipleSubscriptionTypes_IndependentManagement() {
        // when
        service.subscribeRealTimeTradeRate(testTicker1);
        service.subscribeRealTimeOhlc(testTicker1);
        service.subscribePrevRate(testTicker1);

        // then
        assertThat(service.isSubscribedToRealTimeTradeRate(testTicker1)).isTrue();
        assertThat(service.isSubscribedToRealTimeOhlc(testTicker1)).isTrue();
        assertThat(service.isSubscribedToPrevRate(testTicker1)).isTrue();

        // when - OHLC만 해지
        service.unsubscribeRealTimeOhlc(testTicker1);

        // then - 다른 구독은 유지됨
        assertThat(service.isSubscribedToRealTimeTradeRate(testTicker1)).isTrue();
        assertThat(service.isSubscribedToRealTimeOhlc(testTicker1)).isFalse();
        assertThat(service.isSubscribedToPrevRate(testTicker1)).isTrue();
    }

    @Test
    @DisplayName("각 구독 타입별로 다른 티커를 구독할 수 있다")
    void differentTickersPerSubscriptionType() {
        // when
        service.subscribeRealTimeTradeRate(testTicker1);
        service.subscribeRealTimeOhlc(testTicker2);
        service.subscribePrevRate(testTicker3);

        // then
        assertThat(service.getAllRealTimeTradeRateSubscribedTickers()).containsOnly(testTicker1);
        assertThat(service.getAllRealTimeOhlcSubscribedTickers()).containsOnly(testTicker2);
        assertThat(service.getAllPrevRateSubscribedTickers("irrelevant")).containsOnly(testTicker3);
    }

    @Test
    @DisplayName("대량의 티커 구독을 효율적으로 처리한다")
    void bulkSubscriptions_EfficientHandling() {
        // given
        String[] tickers = new String[100];
        for (int i = 0; i < 100; i++) {
            tickers[i] = "TICKER_" + i;
        }

        // when
        for (String ticker : tickers) {
            service.subscribeRealTimeTradeRate(ticker);
            service.subscribeRealTimeOhlc(ticker);
            service.subscribePrevRate(ticker);
        }

        // then
        assertThat(service.getAllRealTimeTradeRateSubscribedTickers()).hasSize(100);
        assertThat(service.getAllRealTimeOhlcSubscribedTickers()).hasSize(100);
        assertThat(service.getAllPrevRateSubscribedTickers("irrelevant")).hasSize(100);

        // 특정 티커들이 모든 타입에 구독되어 있는지 확인
        assertThat(service.isSubscribedToRealTimeTradeRate("TICKER_50")).isTrue();
        assertThat(service.isSubscribedToRealTimeOhlc("TICKER_50")).isTrue();
        assertThat(service.isSubscribedToPrevRate("TICKER_50")).isTrue();
    }

    // ===== 입력 검증 테스트 =====
    @Test
    @DisplayName("null 티커로 구독 시 예외가 발생한다")
    void subscribeWithNullTicker_ThrowsException() {
        // when & then
        assertThatThrownBy(() -> service.subscribeRealTimeTradeRate(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("유효하지 않은 티커입니다");

        assertThatThrownBy(() -> service.subscribeRealTimeOhlc(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("유효하지 않은 티커입니다");

        assertThatThrownBy(() -> service.subscribePrevRate(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("유효하지 않은 티커입니다");
    }

    @Test
    @DisplayName("빈 문자열 티커로 구독 시 예외가 발생한다")
    void subscribeWithEmptyTicker_ThrowsException() {
        // when & then
        assertThatThrownBy(() -> service.subscribeRealTimeTradeRate(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("유효하지 않은 티커입니다");

        assertThatThrownBy(() -> service.subscribeRealTimeOhlc("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("유효하지 않은 티커입니다");

        assertThatThrownBy(() -> service.subscribePrevRate("\t\n"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("유효하지 않은 티커입니다");
    }

    @Test
    @DisplayName("null 티커로 구독 해지 시 예외가 발생한다")
    void unsubscribeWithNullTicker_ThrowsException() {
        // when & then
        assertThatThrownBy(() -> service.unsubscribeRealTimeTradeRate(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("유효하지 않은 티커입니다");

        assertThatThrownBy(() -> service.unsubscribeRealTimeOhlc(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("유효하지 않은 티커입니다");

        assertThatThrownBy(() -> service.unsubscribePrevRate(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("유효하지 않은 티커입니다");
    }

    @Test
    @DisplayName("유효하지 않은 티커의 구독 상태 확인 시 예외가 발생한다")
    void isSubscribedWithInvalidTicker_ThrowsException() {
        assertThatThrownBy(() -> service.isSubscribedToRealTimeTradeRate(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("유효하지 않은 티커입니다");

        assertThatThrownBy(() -> service.isSubscribedToRealTimeTradeRate(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("유효하지 않은 티커입니다");

        assertThatThrownBy(() -> service.isSubscribedToRealTimeTradeRate("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("유효하지 않은 티커입니다");

    }

    // ===== 엣지 케이스 테스트 =====
    @Test
    @DisplayName("공백이 포함된 유효한 티커는 정상적으로 처리된다")
    void subscribeWithValidTickerContainingSpaces_HandledCorrectly() {
        // given
        String tickerWithSpaces = " BTC ";

        // when
        service.subscribeRealTimeTradeRate(tickerWithSpaces);

        // then
        assertThat(service.isSubscribedToRealTimeTradeRate(tickerWithSpaces)).isTrue();
        assertThat(service.isSubscribedToRealTimeTradeRate("BTC")).isFalse(); // 공백 포함은 다른 키
    }

    @Test
    @DisplayName("대소문자가 다른 티커는 서로 다른 구독으로 처리된다")
    void subscribeWithDifferentCase_TreatedAsDifferent() {
        // when
        service.subscribeRealTimeTradeRate("BTC");
        service.subscribeRealTimeTradeRate("btc");
        service.subscribeRealTimeTradeRate("Btc");

        // then
        assertThat(service.getAllRealTimeTradeRateSubscribedTickers()).hasSize(3);
        assertThat(service.isSubscribedToRealTimeTradeRate("BTC")).isTrue();
        assertThat(service.isSubscribedToRealTimeTradeRate("btc")).isTrue();
        assertThat(service.isSubscribedToRealTimeTradeRate("Btc")).isTrue();
    }

    @Test
    @DisplayName("특수 문자가 포함된 티커도 정상적으로 처리된다")
    void subscribeWithSpecialCharacters_HandledCorrectly() {
        // given
        String specialTicker1 = "BTC-USD";
        String specialTicker2 = "ETH/USDT";
        String specialTicker3 = "DOT_BTC";

        // when
        service.subscribeRealTimeTradeRate(specialTicker1);
        service.subscribeRealTimeOhlc(specialTicker2);
        service.subscribePrevRate(specialTicker3);

        // then
        assertThat(service.isSubscribedToRealTimeTradeRate(specialTicker1)).isTrue();
        assertThat(service.isSubscribedToRealTimeOhlc(specialTicker2)).isTrue();
        assertThat(service.isSubscribedToPrevRate(specialTicker3)).isTrue();
    }

    // ===== 동시성 테스트 (단위 테스트 수준) =====
    @Test
    @DisplayName("동일한 구독 타입에서 여러 티커를 동시에 관리할 수 있다")
    void concurrentTickerManagement_SameSubscriptionType() {
        // when
        service.subscribeRealTimeTradeRate(testTicker1);
        service.subscribeRealTimeTradeRate(testTicker2);
        service.subscribeRealTimeTradeRate(testTicker3);

        // then
        Set<String> subscriptions = service.getAllRealTimeTradeRateSubscribedTickers();
        assertThat(subscriptions).hasSize(3);
        assertThat(subscriptions).containsExactlyInAnyOrder(testTicker1, testTicker2, testTicker3);

        // when - 일부 해지
        service.unsubscribeRealTimeTradeRate(testTicker2);

        // then
        Set<String> updatedSubscriptions = service.getAllRealTimeTradeRateSubscribedTickers();
        assertThat(updatedSubscriptions).hasSize(2);
        assertThat(updatedSubscriptions).containsExactlyInAnyOrder(testTicker1, testTicker3);
        assertThat(updatedSubscriptions).doesNotContain(testTicker2);
    }

    // ===== 메서드 시그니처 이슈 테스트 =====
    @Test
    @DisplayName("getAllPrevRateSubscribedTickers 메서드의 파라미터는 실제로 사용되지 않는다")
    void getAllPrevRateSubscribedTickers_ParameterNotUsed() {
        // given
        service.subscribePrevRate(testTicker1);
        service.subscribePrevRate(testTicker2);

        // when - 다른 파라미터로 호출해도 같은 결과
        Set<String> result1 = service.getAllPrevRateSubscribedTickers(testTicker1);
        Set<String> result2 = service.getAllPrevRateSubscribedTickers(testTicker2);
        Set<String> result3 = service.getAllPrevRateSubscribedTickers("NONEXISTENT");

        // then - 모든 호출이 동일한 결과 반환
        assertThat(result1).isEqualTo(result2);
        assertThat(result2).isEqualTo(result3);
        assertThat(result1).containsExactlyInAnyOrder(testTicker1, testTicker2);
    }

    // ===== 비즈니스 로직 일관성 테스트 =====
    @Test
    @DisplayName("구독과 해지가 순서에 관계없이 일관되게 동작한다")
    void subscriptionLifecycle_ConsistentBehavior() {
        // 초기 상태 확인
        assertThat(service.isSubscribedToRealTimeTradeRate(testTicker1)).isFalse();

        // 구독 -> 확인 -> 해지 -> 확인
        service.subscribeRealTimeTradeRate(testTicker1);
        assertThat(service.isSubscribedToRealTimeTradeRate(testTicker1)).isTrue();

        service.unsubscribeRealTimeTradeRate(testTicker1);
        assertThat(service.isSubscribedToRealTimeTradeRate(testTicker1)).isFalse();

        // 재구독
        service.subscribeRealTimeTradeRate(testTicker1);
        assertThat(service.isSubscribedToRealTimeTradeRate(testTicker1)).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 구독을 해지해도 예외가 발생하지 않는다")
    void unsubscribeNonExistentSubscription_NoException() {
        // when & then - 예외 발생하지 않음
        assertThatCode(() -> {
            service.unsubscribeRealTimeTradeRate(testTicker1);
            service.unsubscribeRealTimeOhlc(testTicker2);
            service.unsubscribePrevRate(testTicker3);
        }).doesNotThrowAnyException();

        // 구독 상태는 여전히 false
        assertThat(service.isSubscribedToRealTimeTradeRate(testTicker1)).isFalse();
        assertThat(service.isSubscribedToRealTimeOhlc(testTicker2)).isFalse();
        assertThat(service.isSubscribedToPrevRate(testTicker3)).isFalse();
    }
}