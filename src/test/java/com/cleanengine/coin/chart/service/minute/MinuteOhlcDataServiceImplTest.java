package com.cleanengine.coin.chart.service.minute;

import com.cleanengine.coin.chart.dto.RealTimeOhlcDto;
import com.cleanengine.coin.chart.repository.MinuteOhlcDataRepository;
import com.cleanengine.coin.trade.entity.Trade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("MinuteOhlcDataServiceImpl 단위 테스트")
class MinuteOhlcDataServiceImplTest {

    @Mock
    private MinuteOhlcDataRepository tradeRepository;

    @InjectMocks
    private MinuteOhlcDataServiceImpl service;

    private List<Trade> mockTrades;
    private String validTicker;

    @BeforeEach
    void setUp() {
        validTicker = "BTC";
        mockTrades = createMockTrades();
    }

    // ===== getMinuteOhlcData 테스트 =====
    @Test
    @DisplayName("정상적인 티커로 분봉 데이터를 조회한다")
    void getMinuteOhlcData_ValidTicker_ReturnsOhlcData() {
        // given
        when(tradeRepository.findByTickerOrderByTradeTimeAsc(validTicker))
                .thenReturn(mockTrades);

        // when
        List<RealTimeOhlcDto> result = service.getMinuteOhlcData(validTicker);

        // then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(2); // 2분간의 데이터

        RealTimeOhlcDto firstMinute = result.getFirst();
        assertThat(firstMinute.getTicker()).isEqualTo("BTC");
        assertThat(firstMinute.getOpen()).isEqualTo(100.0);
        assertThat(firstMinute.getHigh()).isEqualTo(150.0);
        assertThat(firstMinute.getLow()).isEqualTo(100.0);
        assertThat(firstMinute.getClose()).isEqualTo(150.0);
        assertThat(firstMinute.getVolume()).isEqualTo(3.0); // 1.0 + 2.0
    }

    @Test
    @DisplayName("거래 데이터가 없으면 빈 리스트를 반환한다")
    void getMinuteOhlcData_NoTrades_ReturnsEmptyList() {
        // given
        when(tradeRepository.findByTickerOrderByTradeTimeAsc(validTicker))
                .thenReturn(List.of());

        // when
        List<RealTimeOhlcDto> result = service.getMinuteOhlcData(validTicker);

        // then
        assertThat(result).isEmpty();
    }

    // ===== validateTicker 테스트 =====
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t", "\n"})
    @DisplayName("잘못된 티커로 검증하면 예외가 발생한다")
    void validateTicker_InvalidTicker_ThrowsException(String invalidTicker) {
        // when & then
        assertThatThrownBy(() -> service.validateTicker(invalidTicker))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("티커는 비어있을 수 없습니다");
    }

    @Test
    @DisplayName("유효한 티커는 검증을 통과한다")
    void validateTicker_ValidTicker_Success() {
        // when & then (예외가 발생하지 않아야 함)
        service.validateTicker("BTC");
        service.validateTicker("ETH-USD");
        service.validateTicker("123");
    }

    // ===== groupTradesByMinute 테스트 =====
    @Test
    @DisplayName("거래 데이터를 분 단위로 그룹핑한다")
    void groupTradesByMinute_ValidTrades_GroupsCorrectly() {
        // when
        Map<LocalDateTime, List<Trade>> result = service.groupTradesByMinute(mockTrades);

        // then
        assertThat(result).hasSize(2);

        LocalDateTime firstMinute = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
        LocalDateTime secondMinute = LocalDateTime.of(2024, 1, 15, 10, 31, 0);

        assertThat(result).containsKey(firstMinute);
        assertThat(result).containsKey(secondMinute);
        assertThat(result.get(firstMinute)).hasSize(2);
        assertThat(result.get(secondMinute)).hasSize(1);
    }

    @Test
    @DisplayName("빈 거래 리스트는 빈 맵을 반환한다")
    void groupTradesByMinute_EmptyTrades_ReturnsEmptyMap() {
        // when
        Map<LocalDateTime, List<Trade>> result = service.groupTradesByMinute(List.of());

        // then
        assertThat(result).isEmpty();
    }

    // ===== truncateToMinute 테스트 =====
    @Test
    @DisplayName("거래 시간을 분 단위로 자른다")
    void truncateToMinute_ValidTrade_TruncatesCorrectly() {
        // given
        Trade trade = createTrade(LocalDateTime.of(2024, 1, 15, 10, 30, 45), 100.0, 1.0);

        // when
        LocalDateTime result = service.truncateToMinute(trade);

        // then
        assertThat(result).isEqualTo(LocalDateTime.of(2024, 1, 15, 10, 30, 0));
    }

    // ===== createOhlcDto 테스트 =====
    @Test
    @DisplayName("단일 분 거래 데이터로 OHLC DTO를 생성한다")
    void createOhlcDto_ValidTrades_CreatesCorrectDto() {
        // given
        String ticker = "BTC";
        LocalDateTime minute = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
        List<Trade> trades = List.of(
                createTrade(LocalDateTime.of(2024, 1, 15, 10, 30, 10), 100.0, 1.0),
                createTrade(LocalDateTime.of(2024, 1, 15, 10, 30, 30), 150.0, 2.0)
        );

        // when
        RealTimeOhlcDto result = service.createOhlcDto(ticker, minute, trades);

        // then
        assertThat(result.getTicker()).isEqualTo("BTC");
        assertThat(result.getTimestamp()).isEqualTo(minute);
        assertThat(result.getOpen()).isEqualTo(100.0);
        assertThat(result.getHigh()).isEqualTo(150.0);
        assertThat(result.getLow()).isEqualTo(100.0);
        assertThat(result.getClose()).isEqualTo(150.0);
        assertThat(result.getVolume()).isEqualTo(3.0);
    }

    // ===== validateTradeList 테스트 =====
    @Test
    @DisplayName("null 거래 리스트는 예외를 발생시킨다")
    void validateTradeList_NullTrades_ThrowsException() {
        // when & then
        assertThatThrownBy(() -> service.validateTradeList(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("거래 데이터가 없습니다");
    }

    @Test
    @DisplayName("빈 거래 리스트는 예외를 발생시킨다")
    void validateTradeList_EmptyTrades_ThrowsException() {
        // when & then
        assertThatThrownBy(() -> service.validateTradeList(List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("거래 데이터가 없습니다");
    }

    @Test
    @DisplayName("유효한 거래 리스트는 검증을 통과한다")
    void validateTradeList_ValidTrades_Success() {
        // when & then (예외가 발생하지 않아야 함)
        service.validateTradeList(mockTrades);
    }

    // ===== calculateOhlcData 정적 메서드 테스트 =====
    @Test
    @DisplayName("단일 거래 데이터로 OHLC를 계산한다")
    void calculateOhlcData_SingleTrade_CalculatesCorrectly() {
        // given
        List<Trade> trades = List.of(
                createTrade(LocalDateTime.now(), 100.0, 5.0)
        );

        // when
        MinuteOhlcDataServiceImpl.OhlcData result =
                MinuteOhlcDataServiceImpl.calculateOhlcData(trades);

        // then
        assertThat(result.open()).isEqualTo(100.0);
        assertThat(result.high()).isEqualTo(100.0);
        assertThat(result.low()).isEqualTo(100.0);
        assertThat(result.close()).isEqualTo(100.0);
        assertThat(result.volume()).isEqualTo(5.0);
    }

    @Test
    @DisplayName("여러 거래 데이터로 OHLC를 계산한다")
    void calculateOhlcData_MultipleTrades_CalculatesCorrectly() {
        // given
        List<Trade> trades = List.of(
                createTrade(LocalDateTime.of(2024, 1, 15, 10, 30, 10), 100.0, 1.0), // open
                createTrade(LocalDateTime.of(2024, 1, 15, 10, 30, 20), 200.0, 2.0), // high
                createTrade(LocalDateTime.of(2024, 1, 15, 10, 30, 30), 50.0, 3.0),  // low
                createTrade(LocalDateTime.of(2024, 1, 15, 10, 30, 40), 150.0, 4.0)  // close
        );

        // when
        MinuteOhlcDataServiceImpl.OhlcData result =
                MinuteOhlcDataServiceImpl.calculateOhlcData(trades);

        // then
        assertThat(result.open()).isEqualTo(100.0);
        assertThat(result.high()).isEqualTo(200.0);
        assertThat(result.low()).isEqualTo(50.0);
        assertThat(result.close()).isEqualTo(150.0);
        assertThat(result.volume()).isEqualTo(10.0); // 1+2+3+4
    }

    @Test
    @DisplayName("동일한 가격의 거래들로 OHLC를 계산한다")
    void calculateOhlcData_SamePriceTrades_CalculatesCorrectly() {
        // given
        List<Trade> trades = List.of(
                createTrade(LocalDateTime.now(), 100.0, 1.0),
                createTrade(LocalDateTime.now(), 100.0, 2.0),
                createTrade(LocalDateTime.now(), 100.0, 3.0)
        );

        // when
        MinuteOhlcDataServiceImpl.OhlcData result =
                MinuteOhlcDataServiceImpl.calculateOhlcData(trades);

        // then
        assertThat(result.open()).isEqualTo(100.0);
        assertThat(result.high()).isEqualTo(100.0);
        assertThat(result.low()).isEqualTo(100.0);
        assertThat(result.close()).isEqualTo(100.0);
        assertThat(result.volume()).isEqualTo(6.0);
    }

    // ===== OhlcData 레코드 테스트 =====
    @Test
    @DisplayName("OhlcData 레코드가 올바르게 동작한다")
    void ohlcDataRecord_WorksCorrectly() {
        // given
        MinuteOhlcDataServiceImpl.OhlcData ohlcData =
                new MinuteOhlcDataServiceImpl.OhlcData(100.0, 200.0, 50.0, 150.0, 10.0);

        // then
        assertThat(ohlcData.open()).isEqualTo(100.0);
        assertThat(ohlcData.high()).isEqualTo(200.0);
        assertThat(ohlcData.low()).isEqualTo(50.0);
        assertThat(ohlcData.close()).isEqualTo(150.0);
        assertThat(ohlcData.volume()).isEqualTo(10.0);
        assertThat(ohlcData.toString()).contains("100.0", "200.0", "50.0", "150.0", "10.0");
    }

    // ===== 경계값 테스트 =====
    @Test
    @DisplayName("소수점 가격과 거래량으로 정확하게 계산한다")
    void calculateOhlcData_DecimalValues_CalculatesCorrectly() {
        // given
        List<Trade> trades = List.of(
                createTrade(LocalDateTime.now(), 100.5, 1.5),
                createTrade(LocalDateTime.now(), 200.75, 2.25)
        );

        // when
        MinuteOhlcDataServiceImpl.OhlcData result =
                MinuteOhlcDataServiceImpl.calculateOhlcData(trades);

        // then
        assertThat(result.open()).isEqualTo(100.5);
        assertThat(result.high()).isEqualTo(200.75);
        assertThat(result.low()).isEqualTo(100.5);
        assertThat(result.close()).isEqualTo(200.75);
        assertThat(result.volume()).isEqualTo(3.75); // 1.5 + 2.25
    }

    // =====리플렉션 제거=====
    private List<Trade> createMockTrades() {
        return List.of(
                // 첫 번째 분 (10:30)
                createTrade(LocalDateTime.of(2024, 1, 15, 10, 30, 10), 100.0, 1.0),
                createTrade(LocalDateTime.of(2024, 1, 15, 10, 30, 30), 150.0, 2.0),

                // 두 번째 분 (10:31)
                createTrade(LocalDateTime.of(2024, 1, 15, 10, 31, 20), 200.0, 3.0)
        );
    }

    /**
     * Trade 엔티티 생성 - @AllArgsConstructor 사용하여 리플렉션 제거
     *
     * @param tradeTime 거래 시간
     * @param price 가격
     * @param size 거래량
     * @return Trade 객체
     */
    private Trade createTrade(LocalDateTime tradeTime, Double price, Double size) {
        return new Trade(
                null,       // id (자동 생성) 실제 db에 들어가는게 아니기때문에 null로 설정
                "BTC",          // ticker
                tradeTime,      // tradeTime
                1,              // buyUserId (더미 값)
                2,              // sellUserId (더미 값)
                price,          // price
                size            // size
        );
    }
}