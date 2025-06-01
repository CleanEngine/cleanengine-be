package com.cleanengine.coin.chart.service;

import com.cleanengine.coin.chart.dto.RealTimeOhlcDto;
import com.cleanengine.coin.trade.entity.Trade;
import com.cleanengine.coin.trade.repository.TradeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RealTimeOhlcService 단위 테스트")
class RealTimeOhlcServiceTest {

    @Mock
    private TradeRepository tradeRepository;

    @InjectMocks
    private RealTimeOhlcService service;

    private String validTicker;
    private LocalDateTime fixedNow;
    private List<Trade> mockTrades;

    @BeforeEach
    void setUp() {
        validTicker = "BTC";
        fixedNow = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
        mockTrades = createMockTrades();
    }

    // ===== getRealTimeOhlc 통합 테스트 =====
    @Test
    @DisplayName("정상적인 거래 데이터로 실시간 OHLC를 생성한다")
    void getRealTimeOhlc_WithValidTrades_ReturnsOhlcData() {
        // given
        when(tradeRepository.findByTickerAndTradeTimeBetweenOrderByTradeTimeAsc(
                eq(validTicker), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(mockTrades);

        // when
        RealTimeOhlcDto result = service.getRealTimeOhlc(validTicker);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTicker()).isEqualTo("BTC");
        assertThat(result.getOpen()).isEqualTo(200.0); // reverse 후 첫 번째
        assertThat(result.getHigh()).isEqualTo(200.0);
        assertThat(result.getLow()).isEqualTo(100.0);
        assertThat(result.getClose()).isEqualTo(100.0); // reverse 후 마지막
        assertThat(result.getVolume()).isEqualTo(6.0); // 1+2+3
    }

    @Test
    @DisplayName("거래 데이터가 없으면 캐시된 데이터를 반환한다")
    void getRealTimeOhlc_NoTrades_ReturnsCachedData() {
        // given
        when(tradeRepository.findByTickerAndTradeTimeBetweenOrderByTradeTimeAsc(
                eq(validTicker), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of());

        // 캐시에 데이터 미리 저장
        RealTimeOhlcDto cachedData = new RealTimeOhlcDto(validTicker, fixedNow, 100.0, 100.0, 100.0, 100.0, 5.0);
        service.updateCache(validTicker, fixedNow, cachedData);

        // when
        RealTimeOhlcDto result = service.getRealTimeOhlc(validTicker);

        // then
        assertThat(result).isEqualTo(cachedData);
    }

    @Test
    @DisplayName("거래 데이터도 캐시도 없으면 null을 반환한다")
    void getRealTimeOhlc_NoTradesNoCache_ReturnsNull() {
        // given
        when(tradeRepository.findByTickerAndTradeTimeBetweenOrderByTradeTimeAsc(
                eq(validTicker), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of());

        // when
        RealTimeOhlcDto result = service.getRealTimeOhlc(validTicker);

        // then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("예외 발생 시 캐시된 데이터를 반환한다")
    void getRealTimeOhlc_ExceptionOccurs_ReturnsCachedData() {
        // given
        when(tradeRepository.findByTickerAndTradeTimeBetweenOrderByTradeTimeAsc(
                eq(validTicker), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenThrow(new RuntimeException("DB 연결 오류"));

        RealTimeOhlcDto cachedData = new RealTimeOhlcDto(validTicker, fixedNow, 100.0, 100.0, 100.0, 100.0, 5.0);
        service.updateCache(validTicker, fixedNow, cachedData);

        // when
        RealTimeOhlcDto result = service.getRealTimeOhlc(validTicker);

        // then
        assertThat(result).isEqualTo(cachedData);
    }

    // ===== calculateTimeRange 테스트 =====
    @Test
    @DisplayName("첫 번째 호출 시 1초 전부터 현재까지의 범위를 계산한다")
    void calculateTimeRange_FirstCall_ReturnsOneSecondRange() {
        // when
        RealTimeOhlcService.TimeRange result = service.calculateTimeRange(validTicker, fixedNow);

        // then
        assertThat(result.start()).isEqualTo(fixedNow.minusSeconds(1));
        assertThat(result.end()).isEqualTo(fixedNow);
    }

    @Test
    @DisplayName("이전 처리 시간이 있으면 그 시간부터 현재까지의 범위를 계산한다")
    void calculateTimeRange_WithPreviousTime_ReturnsCustomRange() {
        // given
        LocalDateTime previousTime = fixedNow.minusSeconds(5);
        service.updateCache(validTicker, previousTime, null); // 이전 시간만 설정

        // when
        RealTimeOhlcService.TimeRange result = service.calculateTimeRange(validTicker, fixedNow);

        // then
        assertThat(result.start()).isEqualTo(previousTime);
        assertThat(result.end()).isEqualTo(fixedNow);
    }

    // ===== getProcessedTradeData 테스트 =====
    @Test
    @DisplayName("거래 데이터를 조회하고 역순으로 정렬한다")
    void getProcessedTradeData_ValidTimeRange_ReturnsReversedTrades() {
        // given
        RealTimeOhlcService.TimeRange timeRange = new RealTimeOhlcService.TimeRange(
                fixedNow.minusSeconds(1), fixedNow);

        when(tradeRepository.findByTickerAndTradeTimeBetweenOrderByTradeTimeAsc(
                validTicker, timeRange.start(), timeRange.end()))
                .thenReturn(mockTrades);

        // when
        List<Trade> result = service.getProcessedTradeData(validTicker, timeRange);

        // then
        assertThat(result).hasSize(3);
        // 역순으로 정렬되었는지 확인 (원래 순서: 100, 150, 200 -> 역순: 200, 150, 100)
        assertThat(result.get(0).getPrice()).isEqualTo(200.0);
        assertThat(result.get(1).getPrice()).isEqualTo(150.0);
        assertThat(result.get(2).getPrice()).isEqualTo(100.0);
    }

    @Test
    @DisplayName("빈 거래 데이터는 빈 리스트를 반환한다")
    void getProcessedTradeData_EmptyTrades_ReturnsEmptyList() {
        // given
        RealTimeOhlcService.TimeRange timeRange = new RealTimeOhlcService.TimeRange(
                fixedNow.minusSeconds(1), fixedNow);

        when(tradeRepository.findByTickerAndTradeTimeBetweenOrderByTradeTimeAsc(
                validTicker, timeRange.start(), timeRange.end()))
                .thenReturn(List.of());

        // when
        List<Trade> result = service.getProcessedTradeData(validTicker, timeRange);

        // then
        assertThat(result).isEmpty();
    }

    // ===== updateCache 테스트 =====
    @Test
    @DisplayName("캐시를 정상적으로 업데이트한다")
    void updateCache_ValidData_UpdatesCorrectly() {
        // given
        RealTimeOhlcDto ohlcData = new RealTimeOhlcDto(validTicker, fixedNow, 100.0, 200.0, 50.0, 150.0, 10.0);

        // when
        service.updateCache(validTicker, fixedNow, ohlcData);

        // then
        RealTimeOhlcDto cachedData = service.getCachedData(validTicker);
        assertThat(cachedData).isEqualTo(ohlcData);

        // 시간 범위 계산 시 업데이트된 시간이 사용되는지 확인
        RealTimeOhlcService.TimeRange timeRange = service.calculateTimeRange(validTicker, fixedNow.plusSeconds(5));
        assertThat(timeRange.start()).isEqualTo(fixedNow);
    }

    // ===== getCachedData 테스트 =====
    @Test
    @DisplayName("캐시된 데이터를 정상적으로 조회한다")
    void getCachedData_ExistingData_ReturnsData() {
        // given
        RealTimeOhlcDto expectedData = new RealTimeOhlcDto(validTicker, fixedNow, 100.0, 200.0, 50.0, 150.0, 10.0);
        service.updateCache(validTicker, fixedNow, expectedData);

        // when
        RealTimeOhlcDto result = service.getCachedData(validTicker);

        // then
        assertThat(result).isEqualTo(expectedData);
    }

    @Test
    @DisplayName("캐시에 데이터가 없으면 null을 반환한다")
    void getCachedData_NoData_ReturnsNull() {
        // when
        RealTimeOhlcDto result = service.getCachedData("NONEXISTENT");

        // then
        assertThat(result).isNull();
    }

    // ===== createOhlcDto 테스트 =====
    @Test
    @DisplayName("OHLCV 데이터로 DTO를 생성한다")
    void createOhlcDto_ValidData_CreatesCorrectDto() {
        // given
        RealTimeOhlcService.calculateOhlcv ohlcv =
                new RealTimeOhlcService.calculateOhlcv(100.0, 200.0, 50.0, 150.0, 10.0);

        // when
        RealTimeOhlcDto result = service.createOhlcDto(validTicker, fixedNow, ohlcv);

        // then
        assertThat(result.getTicker()).isEqualTo(validTicker);
        assertThat(result.getTimestamp()).isEqualTo(fixedNow);
        assertThat(result.getOpen()).isEqualTo(100.0);
        assertThat(result.getHigh()).isEqualTo(200.0);
        assertThat(result.getLow()).isEqualTo(50.0);
        assertThat(result.getClose()).isEqualTo(150.0);
        assertThat(result.getVolume()).isEqualTo(10.0);
    }

    // ===== getCalculateOhlcv 정적 메서드 테스트 =====
    @Test
    @DisplayName("단일 거래로 OHLCV를 계산한다")
    void getCalculateOhlcv_SingleTrade_CalculatesCorrectly() {
        // given
        List<Trade> trades = List.of(
                createTrade(fixedNow, 100.0, 5.0)
        );

        // when
        RealTimeOhlcService.calculateOhlcv result =
                RealTimeOhlcService.getCalculateOhlcv(trades);

        // then
        assertThat(result.open()).isEqualTo(100.0);
        assertThat(result.high()).isEqualTo(100.0);
        assertThat(result.low()).isEqualTo(100.0);
        assertThat(result.close()).isEqualTo(100.0);
        assertThat(result.volume()).isEqualTo(5.0);
    }

    @Test
    @DisplayName("여러 거래로 OHLCV를 계산한다")
    void getCalculateOhlcv_MultipleTrades_CalculatesCorrectly() {
        // given
        List<Trade> trades = List.of(
                createTrade(fixedNow.minusSeconds(3), 100.0, 1.0), // open
                createTrade(fixedNow.minusSeconds(2), 200.0, 2.0), // high
                createTrade(fixedNow.minusSeconds(1), 50.0, 3.0),  // low
                createTrade(fixedNow, 150.0, 4.0)                  // close
        );

        // when
        RealTimeOhlcService.calculateOhlcv result =
                RealTimeOhlcService.getCalculateOhlcv(trades);

        // then
        assertThat(result.open()).isEqualTo(100.0);
        assertThat(result.high()).isEqualTo(200.0);
        assertThat(result.low()).isEqualTo(50.0);
        assertThat(result.close()).isEqualTo(150.0);
        assertThat(result.volume()).isEqualTo(10.0); // 1+2+3+4
    }

    @Test
    @DisplayName("동일한 가격의 거래들로 OHLCV를 계산한다")
    void getCalculateOhlcv_SamePriceTrades_CalculatesCorrectly() {
        // given
        List<Trade> trades = List.of(
                createTrade(fixedNow.minusSeconds(2), 100.0, 1.0),
                createTrade(fixedNow.minusSeconds(1), 100.0, 2.0),
                createTrade(fixedNow, 100.0, 3.0)
        );

        // when
        RealTimeOhlcService.calculateOhlcv result =
                RealTimeOhlcService.getCalculateOhlcv(trades);

        // then
        assertThat(result.open()).isEqualTo(100.0);
        assertThat(result.high()).isEqualTo(100.0);
        assertThat(result.low()).isEqualTo(100.0);
        assertThat(result.close()).isEqualTo(100.0);
        assertThat(result.volume()).isEqualTo(6.0);
    }

    @Test
    @DisplayName("소수점 가격과 거래량으로 정확하게 계산한다")
    void getCalculateOhlcv_DecimalValues_CalculatesCorrectly() {
        // given
        List<Trade> trades = List.of(
                createTrade(fixedNow.minusSeconds(1), 100.5, 1.5),
                createTrade(fixedNow, 200.75, 2.25)
        );

        // when
        RealTimeOhlcService.calculateOhlcv result =
                RealTimeOhlcService.getCalculateOhlcv(trades);

        // then
        assertThat(result.open()).isEqualTo(100.5);
        assertThat(result.high()).isEqualTo(200.75);
        assertThat(result.low()).isEqualTo(100.5);
        assertThat(result.close()).isEqualTo(200.75);
        assertThat(result.volume()).isEqualTo(3.75); // 1.5 + 2.25
    }

    // ===== 레코드 객체 테스트 =====
    @Test
    @DisplayName("TimeRange 레코드가 올바르게 동작한다")
    void timeRangeRecord_WorksCorrectly() {
        // given
        LocalDateTime start = fixedNow.minusSeconds(1);
        LocalDateTime end = fixedNow;

        // when
        RealTimeOhlcService.TimeRange timeRange = new RealTimeOhlcService.TimeRange(start, end);

        // then
        assertThat(timeRange.start()).isEqualTo(start);
        assertThat(timeRange.end()).isEqualTo(end);
        assertThat(timeRange.toString()).contains(start.toString(), end.toString());
    }

    @Test
    @DisplayName("calculateOhlcv 레코드가 올바르게 동작한다")
    void calculateOhlcvRecord_WorksCorrectly() {
        // given
        RealTimeOhlcService.calculateOhlcv ohlcv =
                new RealTimeOhlcService.calculateOhlcv(100.0, 200.0, 50.0, 150.0, 10.0);

        // then
        assertThat(ohlcv.open()).isEqualTo(100.0);
        assertThat(ohlcv.high()).isEqualTo(200.0);
        assertThat(ohlcv.low()).isEqualTo(50.0);
        assertThat(ohlcv.close()).isEqualTo(150.0);
        assertThat(ohlcv.volume()).isEqualTo(10.0);
        assertThat(ohlcv.toString()).contains("100.0", "200.0", "50.0", "150.0", "10.0");
    }

    // ===== 동시성 테스트 =====
    @Test
    @DisplayName("여러 티커를 동시에 처리해도 캐시가 올바르게 동작한다")
    void concurrentTickers_CacheWorksCorrectly() {
        // given
        String ticker1 = "BTC";
        String ticker2 = "ETH";
        RealTimeOhlcDto data1 = new RealTimeOhlcDto(ticker1, fixedNow, 100.0, 100.0, 100.0, 100.0, 5.0);
        RealTimeOhlcDto data2 = new RealTimeOhlcDto(ticker2, fixedNow, 200.0, 200.0, 200.0, 200.0, 10.0);

        // when
        service.updateCache(ticker1, fixedNow, data1);
        service.updateCache(ticker2, fixedNow, data2);

        // then
        assertThat(service.getCachedData(ticker1)).isEqualTo(data1);
        assertThat(service.getCachedData(ticker2)).isEqualTo(data2);
        assertThat(service.getCachedData(ticker1)).isNotEqualTo(data2);
    }

    // ===== Repository 호출 검증 테스트 =====
    @Test
    @DisplayName("Repository가 올바른 파라미터로 호출된다")
    void repository_CalledWithCorrectParameters() {
        // given
        when(tradeRepository.findByTickerAndTradeTimeBetweenOrderByTradeTimeAsc(
                any(String.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(mockTrades);

        // when
        service.getRealTimeOhlc(validTicker);

        // then
        ArgumentCaptor<String> tickerCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<LocalDateTime> startCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> endCaptor = ArgumentCaptor.forClass(LocalDateTime.class);

        verify(tradeRepository).findByTickerAndTradeTimeBetweenOrderByTradeTimeAsc(
                tickerCaptor.capture(), startCaptor.capture(), endCaptor.capture());

        assertThat(tickerCaptor.getValue()).isEqualTo(validTicker);
        assertThat(startCaptor.getValue()).isBefore(endCaptor.getValue());
    }

    // ===== 헬퍼 메서드들 =====
    private List<Trade> createMockTrades() {
        return List.of(
                createTrade(fixedNow.minusSeconds(3), 100.0, 1.0),
                createTrade(fixedNow.minusSeconds(2), 150.0, 2.0),
                createTrade(fixedNow.minusSeconds(1), 200.0, 3.0)
        );
    }

    private Trade createTrade(LocalDateTime tradeTime, Double price, Double size) {
        Trade trade = new Trade();
        try {
            setField(trade, "tradeTime", tradeTime);
            setField(trade, "price", price);
            setField(trade, "size", size);
        } catch (Exception e) {
            throw new RuntimeException("Trade 객체 생성 실패", e);
        }
        return trade;
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        java.lang.reflect.Field field = Trade.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}