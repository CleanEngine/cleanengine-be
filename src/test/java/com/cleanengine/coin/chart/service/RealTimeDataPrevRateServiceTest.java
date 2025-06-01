package com.cleanengine.coin.chart.service;

import com.cleanengine.coin.chart.dto.PrevRateDto;
import com.cleanengine.coin.chart.dto.TradeEventDto;
import com.cleanengine.coin.chart.repository.RealTimeTradeRepository;
import com.cleanengine.coin.trade.entity.Trade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RealTimeDataPrevRateService 테스트")
class RealTimeDataPrevRateServiceTest {

    @Mock
    private RealTimeTradeRepository tradeRepository;

    @InjectMocks
    private RealTimeDataPrevRateService service;

    private TradeEventDto tradeEventDto;
    private LocalDateTime currentTime;
    private Trade mockTrade;

    @BeforeEach
    void setUp() {
        tradeEventDto = new TradeEventDto("TRUMP", 0,150.0, LocalDateTime.now());
        currentTime = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
        mockTrade = createMockTrade();
    }

    @Test
    @DisplayName("전일 거래 데이터가 있을 때 정상적으로 PrevRateDto를 생성한다")
    void generatePrevRateData_WithYesterdayTrade_Success() {
        // given
        when(tradeRepository.findFirstByTickerAndTradeTimeBetweenOrderByTradeTimeDesc(
                eq("TRUMP"), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(mockTrade);

        // when
        PrevRateDto result = service.generatePrevRateData(tradeEventDto, currentTime);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTicker()).isEqualTo("TRUMP");
        assertThat(result.getCurrentPrice()).isEqualTo(150.0);
        assertThat(result.getPrevClose()).isEqualTo(100.0);
        assertThat(result.getChangeRate()).isEqualTo(50.0); // (150-100)/100 * 100
        assertThat(result.getTimestamp()).isEqualTo(currentTime);
    }

    @Test
    @DisplayName("전일 거래 데이터가 없을 때 기본값으로 PrevRateDto를 생성한다")
    void generatePrevRateData_WithoutYesterdayTrade_ReturnsDefault() {
        // given
        when(tradeRepository.findFirstByTickerAndTradeTimeBetweenOrderByTradeTimeDesc(
                eq("TRUMP"), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(null);

        // when
        PrevRateDto result = service.generatePrevRateData(tradeEventDto, currentTime);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTicker()).isEqualTo("TRUMP");
        assertThat(result.getCurrentPrice()).isEqualTo(150.0);
        assertThat(result.getPrevClose()).isEqualTo(0.0);
        assertThat(result.getChangeRate()).isEqualTo(0.0);
        assertThat(result.getTimestamp()).isEqualTo(currentTime);
    }

    @Test
    @DisplayName("현재 시간을 사용하는 오버로드 메서드가 정상 동작한다")
    void generatePrevRateData_WithCurrentTime_Success() {
        // given
        when(tradeRepository.findFirstByTickerAndTradeTimeBetweenOrderByTradeTimeDesc(
                eq("TRUMP"), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(mockTrade);

        // when
        PrevRateDto result = service.generatePrevRateData(tradeEventDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTicker()).isEqualTo("TRUMP");
        assertThat(result.getCurrentPrice()).isEqualTo(150.0);
        assertThat(result.getPrevClose()).isEqualTo(100.0);
    }

    @Test
    @DisplayName("변화율이 양수일때 제대로 츨력이 되는지")
    void getChangeRate_PriceIncrease_CalculatesCorrectly() {
        // when
        double result = RealTimeDataPrevRateService.getChangeRate(120.0, 100.0);

        // then
        assertThat(result).isEqualTo(20.0); // (120-100)/100 * 100
    }

    @Test
    @DisplayName("변화율이 음수일때 로직이 정상적으로 작동하는지")
    void getChangeRate_PriceDecrease_CalculatesCorrectly() {
        // when
        double result = RealTimeDataPrevRateService.getChangeRate(80.0, 100.0);

        // then
        assertThat(result).isEqualTo(-20.0); // (80-100)/100 * 100
    }

    @Test
    @DisplayName("변화율이 0일때 0으로 제대로 출력이 되는지")
    void getChangeRate_SamePrice_ReturnsZero() {
        // when
        double result = RealTimeDataPrevRateService.getChangeRate(100.0, 100.0);

        // then
        assertThat(result).isEqualTo(0.0);
    }

    @Test
    @DisplayName("전일 시간 범위를 올바르게 계산한다")
    void getYesterDay_CalculatesCorrectRange() {
        // given
        LocalDateTime today = LocalDateTime.of(2024, 1, 15, 14, 30, 45);

        // when
        RealTimeDataPrevRateService.YesterDay result = RealTimeDataPrevRateService.getYesterDay(today);

        // then
        assertThat(result.yesterdayStart()).isEqualTo(LocalDateTime.of(2024, 1, 14, 0, 0, 0));
        assertThat(result.yesterdayEnd()).isEqualTo(LocalDateTime.of(2024, 1, 14, 23, 59, 59));
    }

    @Test
    @DisplayName("YesterDay 레코드가 올바르게 동작한다")
    void yesterDayRecord_WorksCorrectly() {
        // given
        LocalDateTime start = LocalDateTime.of(2024, 1, 14, 0, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 14, 23, 59, 59);

        // when
        RealTimeDataPrevRateService.YesterDay yesterDay = new RealTimeDataPrevRateService.YesterDay(start, end);

        // then
        assertThat(yesterDay.yesterdayStart()).isEqualTo(start);
        assertThat(yesterDay.yesterdayEnd()).isEqualTo(end);
        assertThat(yesterDay.toString()).contains("2024-01-14T00:00");
        assertThat(yesterDay.toString()).contains("2024-01-14T23:59:59");
    }

    @Test
    @DisplayName("소수점이 있는 가격에서도 변화율이 정확하게 계산이 된다")
    void getChangeRate_WithDecimalPrices_CalculatesCorrectly() {
        // when
        double result = RealTimeDataPrevRateService.getChangeRate(150.75, 100.50);

        // then
        double expected = ((150.75 - 100.50) / 100.50) * 100;
        assertThat(result).isEqualTo(expected);
    }

    private Trade createMockTrade() {
        // Trade 엔티티 생성 (실제 구현에 따라 수정 필요)
        Trade trade = new Trade();
        // setPrice가 있다고 가정하거나, 빌더 패턴 사용
        // trade.setPrice(100.0);
        // 또는 리플렉션을 사용하여 필드 설정
        try {
            java.lang.reflect.Field priceField = Trade.class.getDeclaredField("price");
            priceField.setAccessible(true);
            priceField.set(trade, 100.0);
        } catch (Exception e) {
            // 실제 Trade 엔티티 구조에 맞게 수정 필요
        }
        return trade;
    }
}