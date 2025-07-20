package com.cleanengine.coin.chart.service;

import com.cleanengine.coin.chart.dto.RealTimeOhlcDto;
import com.cleanengine.coin.trade.application.port.in.TradeQueryUseCase;
import com.cleanengine.coin.trade.domain.model.Trade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RealTimeOhlcServiceTest {

    @Mock
    private TradeQueryUseCase tradeQueryUseCase;

    @InjectMocks
    private RealTimeOhlcService realTimeOhlcService;

    private final String ticker = "KRW-BTC";
    private final Integer dummyBuyUserId = 100;
    private final Integer dummySellUserId = 200;

    @Test
    @DisplayName("새로운 1분봉 - 첫 거래 발생 시 OHLCV가 정상적으로 생성되어야 한다")
    void should_create_new_ohlc_when_new_minute_starts() {
        // given
        LocalDateTime now = LocalDateTime.of(2025, 6, 22, 10, 1, 15);
        LocalDateTime minuteStart = now.truncatedTo(ChronoUnit.MINUTES);

         List<Trade> trades = List.of(
                new Trade(ticker, now.minusSeconds(10), dummyBuyUserId, dummySellUserId, 50000.0, 10.0) // 10:01:05
        );

        when(tradeQueryUseCase.findByTickerAndTradeTimeBetweenOrderByTradeTimeAsc(eq(ticker), eq(minuteStart), eq(now)))
                .thenReturn(trades);

        // when
        RealTimeOhlcDto result = realTimeOhlcService.getAndUpdateCumulative1mOhlc(ticker, now);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getOpen()).isEqualTo(50000.0);
        assertThat(result.getHigh()).isEqualTo(50000.0);
        assertThat(result.getLow()).isEqualTo(50000.0);
        assertThat(result.getClose()).isEqualTo(50000.0);
        assertThat(result.getVolume()).isEqualTo(10.0);
    }

    @Test
    @DisplayName("기존 1분봉 - 새로운 거래 발생 시 OHLCV가 누적 업데이트되어야 한다")
    void should_update_existing_ohlc_on_new_trades_in_same_minute() {
        // given
        // 1. 첫 번째 거래 발생 (10:01:15)
        LocalDateTime time1 = LocalDateTime.of(2025, 6, 22, 10, 1, 15);
        LocalDateTime minuteStart = time1.truncatedTo(ChronoUnit.MINUTES);

        List<Trade> initialTrades = List.of(
                new Trade(ticker, time1.minusSeconds(10), dummyBuyUserId, dummySellUserId, 50000.0, 10.0) // 10:01:05
        );
        when(tradeQueryUseCase.findByTickerAndTradeTimeBetweenOrderByTradeTimeAsc(eq(ticker), eq(minuteStart), eq(time1)))
                .thenReturn(initialTrades);

        realTimeOhlcService.getAndUpdateCumulative1mOhlc(ticker, time1); // 첫 호출로 상태 초기화

        // 2. 두 번째 거래들 발생 (10:01:30)
        LocalDateTime time2 = LocalDateTime.of(2025, 6, 22, 10, 1, 30);

        List<Trade> newTrades = List.of(
                new Trade(ticker, time2.minusSeconds(10), dummyBuyUserId, dummySellUserId, 52000.0, 5.0),  // 10:01:20 (고가)
                new Trade(ticker, time2.minusSeconds(5), dummyBuyUserId, dummySellUserId, 49000.0, 8.0)   // 10:01:25 (저가, 종가)
        );
        when(tradeQueryUseCase.findByTickerAndTradeTimeBetweenOrderByTradeTimeAsc(eq(ticker), eq(time1), eq(time2)))
                .thenReturn(newTrades);

        // when
        RealTimeOhlcDto result = realTimeOhlcService.getAndUpdateCumulative1mOhlc(ticker, time2);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getOpen()).isEqualTo(50000.0); // 시가 불변
        assertThat(result.getHigh()).isEqualTo(52000.0); // 고가 갱신
        assertThat(result.getLow()).isEqualTo(49000.0);  // 저가 갱신
        assertThat(result.getClose()).isEqualTo(49000.0); // 종가 갱신
        assertThat(result.getVolume()).isEqualTo(23.0); // 거래량 누적 (10 + 5 + 8)
    }

    @Test
    @DisplayName("시간 경과 - 다음 '분'으로 넘어갈 시 새로운 1분봉이 시작되어야 한다")
    void should_start_new_ohlc_when_minute_rolls_over() {
        // given
        // 1. 10:01분대의 마지막 상태 설정
        LocalDateTime time1 = LocalDateTime.of(2025, 6, 22, 10, 1, 50);
        LocalDateTime minute1Start = time1.truncatedTo(ChronoUnit.MINUTES);

        List<Trade> tradesInMin1 = List.of(
                new Trade(ticker, time1.minusSeconds(10), dummyBuyUserId, dummySellUserId, 50000.0, 10.0)
        );
        when(tradeQueryUseCase.findByTickerAndTradeTimeBetweenOrderByTradeTimeAsc(eq(ticker), eq(minute1Start), eq(time1)))
                .thenReturn(tradesInMin1);
        realTimeOhlcService.getAndUpdateCumulative1mOhlc(ticker, time1);

        // 2. 10:02분대의 첫 거래 발생
        LocalDateTime time2 = LocalDateTime.of(2025, 6, 22, 10, 2, 10);
        LocalDateTime minute2Start = time2.truncatedTo(ChronoUnit.MINUTES);

        List<Trade> tradesInMin2 = List.of(
                new Trade(ticker, time2.minusSeconds(5), dummyBuyUserId, dummySellUserId, 51500.0, 20.0)
        );
        when(tradeQueryUseCase.findByTickerAndTradeTimeBetweenOrderByTradeTimeAsc(eq(ticker), eq(minute2Start), eq(time2)))
                .thenReturn(tradesInMin2);

        // when
        RealTimeOhlcDto result = realTimeOhlcService.getAndUpdateCumulative1mOhlc(ticker, time2);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getOpen()).isEqualTo(51500.0); // 시가 새로 설정
        assertThat(result.getHigh()).isEqualTo(51500.0);
        assertThat(result.getLow()).isEqualTo(51500.0);
        assertThat(result.getClose()).isEqualTo(51500.0);
        assertThat(result.getVolume()).isEqualTo(20.0); // 거래량 새로 시작
    }
}