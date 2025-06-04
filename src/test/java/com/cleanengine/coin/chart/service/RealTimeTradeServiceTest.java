package com.cleanengine.coin.chart.service;

import com.cleanengine.coin.chart.dto.RealTimeDataDto;
import com.cleanengine.coin.chart.dto.TradeEventDto;
import org.assertj.core.api.AssertProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RealTimeTradeService 단위 테스트")
class RealTimeTradeServiceTest {

    private RealTimeTradeService service;
    private TradeEventDto testTradeEventDto;
    private LocalDateTime testTime;

    @BeforeEach
    void setUp() {
        service = new RealTimeTradeService();
        testTime = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
        testTradeEventDto = new TradeEventDto("TRUMP", 1.5, 50000.0, testTime);
    }

    @Test
    @DisplayName("extractTradeInfo - 정상적인 거래 정보 추출")
    void extractTradeInfo_ValidData_ReturnsCorrectTradeInfo() {
        // when
        RealTimeTradeService.TradeInfo result = service.extractTradeInfo(testTradeEventDto);

        // then
        assertThat(result.ticker()).isEqualTo("TRUMP");
        assertThat(result.price()).isEqualTo(50000.0);
        assertThat(result.size()).isEqualTo(1.5);
        assertThat(result.timestamp()).isEqualTo(testTime);
    }

    @Test
    @DisplayName("extractTradeInfo - null 타임스탬프 처리")
    void extractTradeInfo_NullTimestamp_HandledCorrectly() {
        // given
        TradeEventDto tradeWithNullTime = new TradeEventDto("TRUMP", 2.0, 3000.0, null);

        // when
        RealTimeTradeService.TradeInfo result = service.extractTradeInfo(tradeWithNullTime);

        // then
        assertThat(result.ticker()).isEqualTo("TRUMP");
        assertThat(result.price()).isEqualTo(3000.0);
        assertThat(result.size()).isEqualTo(2.0);
        assertThat(result.timestamp()).isNull();
    }

    // ===== shouldCalculateChangeRate 메서드 테스트 =====
    @Test
    @DisplayName("shouldCalculateChangeRate - 이전 거래가 null인 경우")
    void shouldCalculateChangeRate_PreviousTradeNull_ReturnsFalse() {
        // when
        boolean result = service.shouldCalculateChangeRate(null, testTradeEventDto);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("shouldCalculateChangeRate - 이전 거래 가격이 0인 경우")
    void shouldCalculateChangeRate_PreviousPriceZero_ReturnsFalse() {
        // given
        TradeEventDto previousTrade = new TradeEventDto("TRUMP", 1.0, 0.0, testTime);

        // when
        boolean result = service.shouldCalculateChangeRate(previousTrade, testTradeEventDto);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("shouldCalculateChangeRate - 이전 거래 가격이 음수인 경우")
    void shouldCalculateChangeRate_PreviousPriceNegative_ReturnsFalse() {
        // given
        TradeEventDto previousTrade = new TradeEventDto("TRUMP", 1.0, -100.0, testTime);

        // when
        boolean result = service.shouldCalculateChangeRate(previousTrade, testTradeEventDto);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("shouldCalculateChangeRate - 동일한 객체 참조인 경우")
    void shouldCalculateChangeRate_SameObjectReference_ReturnsFalse() {
        // when
        boolean result = service.shouldCalculateChangeRate(testTradeEventDto, testTradeEventDto);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("shouldCalculateChangeRate - 정상적인 조건인 경우")
    void shouldCalculateChangeRate_ValidCondition_ReturnsTrue() {
        // given
        TradeEventDto previousTrade = new TradeEventDto("TRUMP", 1.0, 45000.0, testTime.minusSeconds(10));

        // when
        boolean result = service.shouldCalculateChangeRate(previousTrade, testTradeEventDto);

        // then
        assertThat(result).isTrue();
    }

    // ===== isNewTrade 메서드 테스트 =====
    @Test
    @DisplayName("isNewTrade - 이전 타임스탬프가 null인 경우")
    void isNewTrade_PreviousTimestampNull_ReturnsTrue() {
        // given
        TradeEventDto previousTrade = new TradeEventDto("TRUMP", 1.0, 45000.0, null);

        // when
        boolean result = service.isNewTrade(previousTrade, testTradeEventDto);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("isNewTrade - 현재 타임스탬프가 null인 경우")
    void isNewTrade_CurrentTimestampNull_ReturnsTrue() {
        // given
        TradeEventDto previousTrade = new TradeEventDto("TRUMP", 1.0, 45000.0, testTime);
        TradeEventDto currentTrade = new TradeEventDto("TRUMP", 1.5, 50000.0, null);

        // when
        boolean result = service.isNewTrade(previousTrade, currentTrade);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("isNewTrade - 둘 다 null인 경우")
    void isNewTrade_BothTimestampsNull_ReturnsTrue() {
        // given
        TradeEventDto previousTrade = new TradeEventDto("TRUMP", 1.0, 45000.0, null);
        TradeEventDto currentTrade = new TradeEventDto("TRUMP", 1.5, 50000.0, null);

        // when
        boolean result = service.isNewTrade(previousTrade, currentTrade);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("isNewTrade - 동일한 타임스탬프인 경우")
    void isNewTrade_SameTimestamp_ReturnsFalse() {
        // given
        TradeEventDto previousTrade = new TradeEventDto("TRUMP", 1.0, 45000.0, testTime);
        TradeEventDto currentTrade = new TradeEventDto("TRUMP", 1.5, 50000.0, testTime);

        // when
        boolean result = service.isNewTrade(previousTrade, currentTrade);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("isNewTrade - 다른 타임스탬프인 경우")
    void isNewTrade_DifferentTimestamp_ReturnsTrue() {
        // given
        TradeEventDto previousTrade = new TradeEventDto("TRUMP", 1.0, 45000.0, testTime.minusSeconds(10));

        // when
        boolean result = service.isNewTrade(previousTrade, testTradeEventDto);

        // then
        assertThat(result).isTrue();
    }

    // ===== createCachedTradeDto 메서드 테스트 =====
    @Test
    @DisplayName("createCachedTradeDto - 정상적인 복사본 생성")
    void createCachedTradeDto_ValidInput_ReturnsCorrectCopy() {
        // when
        TradeEventDto result = service.createCachedTradeDto(testTradeEventDto);

        // then
        assertThat(result.getTicker()).isEqualTo(testTradeEventDto.getTicker());
        assertThat(result.getPrice()).isEqualTo(testTradeEventDto.getPrice());
        assertThat(result.getSize()).isEqualTo(testTradeEventDto.getSize());
        assertThat(result.getTimestamp()).isEqualTo(testTradeEventDto.getTimestamp());
        // 다른 객체임을 확인
        assertThat(result).isNotSameAs(testTradeEventDto);
    }

    @Test
    @DisplayName("createCachedTradeDto - null 타임스탬프 복사")
    void createCachedTradeDto_NullTimestamp_CopiedCorrectly() {
        // given
        TradeEventDto tradeWithNullTime = new TradeEventDto("TRUMP", 2.0, 3000.0, null);

        // when
        TradeEventDto result = service.createCachedTradeDto(tradeWithNullTime);

        // then
        assertThat(result.getTimestamp()).isNull();
        assertThat(result.getTicker()).isEqualTo("TRUMP");
    }

    // ===== createRealTimeDataDto 메서드 테스트 =====
    @Test
    @DisplayName("createRealTimeDataDto - 정상적인 DTO 생성")
    void createRealTimeDataDto_ValidInput_ReturnsCorrectDto() {
        // given
        RealTimeTradeService.TradeInfo tradeInfo = new RealTimeTradeService.TradeInfo(
                "TRUMP", 50000.0, 1.5, testTime);
        double changeRate = 5.5;

        // when
        RealTimeDataDto result = service.createRealTimeDataDto(tradeInfo, changeRate);

        // then
        assertThat(result.getTicker()).isEqualTo("TRUMP");
        assertThat(result.getPrice()).isEqualTo(50000.0);
        assertThat(result.getSize()).isEqualTo(1.5);
        assertThat(result.getChangeRate()).isEqualTo(5.5);
        assertThat(result.getTimestamp()).isEqualTo(testTime);
        assertThat(result.getTransactionId()).isNotNull();
    }

    @Test
    @DisplayName("createRealTimeDataDto - 0 변동률 처리")
    void createRealTimeDataDto_ZeroChangeRate_HandledCorrectly() {
        // given
        RealTimeTradeService.TradeInfo tradeInfo = new RealTimeTradeService.TradeInfo(
                "TRUMP", 3000.0, 2.0, testTime);

        // when
        RealTimeDataDto result = service.createRealTimeDataDto(tradeInfo, 0.0);

        // then
        assertThat(result.getChangeRate()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("createRealTimeDataDto - 음수 변동률 처리")
    void createRealTimeDataDto_NegativeChangeRate_HandledCorrectly() {
        // given
        RealTimeTradeService.TradeInfo tradeInfo = new RealTimeTradeService.TradeInfo(
                "TRUMP", 3000.0, 2.0, testTime);

        // when
        RealTimeDataDto result = service.createRealTimeDataDto(tradeInfo, -10.5);

        // then
        assertThat(result.getChangeRate()).isEqualTo(-10.5);
    }

    // ===== generateTransactionId 메서드 테스트 =====
    @Test
    @DisplayName("generateTransactionId - 유효한 UUID 생성")
    void generateTransactionId_ReturnsValidUUID() {
        // when
        String result = service.generateTransactionId();

        // then
        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();
        // UUID 형식 검증
        assertThat((AssertProvider<UUID>) () -> UUID.fromString(result)).getMostSignificantBits();
    }

    @Test
    @DisplayName("generateTransactionId - 호출할 때마다 다른 ID 생성")
    void generateTransactionId_GeneratesDifferentIds() {
        // when
        String id1 = service.generateTransactionId();
        String id2 = service.generateTransactionId();

        // then
        assertThat(id1).isNotEqualTo(id2);
    }

    @Test
    @DisplayName("generateTransactionId - 연속 호출 시 모두 다른 ID")
    void generateTransactionId_MultipleCallsGenerateDifferentIds() {
        // when
        String id1 = service.generateTransactionId();
        String id2 = service.generateTransactionId();
        String id3 = service.generateTransactionId();

        // then
        assertThat(id1).isNotEqualTo(id2);
        assertThat(id2).isNotEqualTo(id3);
        assertThat(id1).isNotEqualTo(id3);
    }

    // ===== getChangeRate 메서드 테스트 =====
    @Test
    @DisplayName("getChangeRate - 가격 상승 케이스")
    void getChangeRate_PriceIncrease_CalculatesCorrectly() {
        // when
        double result = service.getChangeRate(110.0, 100.0);

        // then
        assertThat(result).isEqualTo(10.0);
    }

    @Test
    @DisplayName("getChangeRate - 가격 하락 케이스")
    void getChangeRate_PriceDecrease_CalculatesCorrectly() {
        // when
        double result = service.getChangeRate(90.0, 100.0);

        // then
        assertThat(result).isEqualTo(-10.0);
    }

    @Test
    @DisplayName("getChangeRate - 가격 변동 없음")
    void getChangeRate_NoChange_ReturnsZero() {
        // when
        double result = service.getChangeRate(100.0, 100.0);

        // then
        assertThat(result).isEqualTo(0.0);
    }

    @Test
    @DisplayName("getChangeRate - 소수점 가격 처리")
    void getChangeRate_DecimalPrices_CalculatesCorrectly() {
        // when
        double result = service.getChangeRate(105.50, 100.25);

        // then
        double expected = ((105.50 - 100.25) / 100.25) * 100;
        assertThat(result).isCloseTo(expected, org.assertj.core.data.Offset.offset(0.0001));
    }

    @Test
    @DisplayName("getChangeRate - 큰 변동률 처리")
    void getChangeRate_LargeChangeRate_CalculatesCorrectly() {
        // when
        double result = service.getChangeRate(200.0, 100.0);

        // then
        assertThat(result).isEqualTo(100.0);
    }

    @Test
    @DisplayName("getChangeRate - 매우 작은 가격 변동")
    void getChangeRate_VerySmallChange_CalculatesCorrectly() {
        // when
        double result = service.getChangeRate(100.01, 100.0);

        // then
        assertThat(result).isCloseTo(0.01, org.assertj.core.data.Offset.offset(0.0001));
    }

    // ===== Record 클래스 테스트 =====
    @Test
    @DisplayName("TradeInfo record - 정상 동작 확인")
    void tradeInfo_Record_WorksCorrectly() {
        // given
        RealTimeTradeService.TradeInfo tradeInfo = new RealTimeTradeService.TradeInfo(
                "TRUMP", 50000.0, 1.5, testTime);

        // then
        assertThat(tradeInfo.ticker()).isEqualTo("TRUMP");
        assertThat(tradeInfo.price()).isEqualTo(50000.0);
        assertThat(tradeInfo.size()).isEqualTo(1.5);
        assertThat(tradeInfo.timestamp()).isEqualTo(testTime);
    }

    @Test
    @DisplayName("TradeInfo record - equals와 hashCode 동작")
    void tradeInfo_Record_EqualsAndHashCode() {
        // given
        RealTimeTradeService.TradeInfo tradeInfo1 = new RealTimeTradeService.TradeInfo(
                "TRUMP", 50000.0, 1.5, testTime);
        RealTimeTradeService.TradeInfo tradeInfo2 = new RealTimeTradeService.TradeInfo(
                "TRUMP", 50000.0, 1.5, testTime);

        // then
        assertThat(tradeInfo1).isEqualTo(tradeInfo2);
        assertThat(tradeInfo1.hashCode()).isEqualTo(tradeInfo2.hashCode());
    }

    @Test
    @DisplayName("ChangeRateResult record - 정상 동작 확인")
    void changeRateResult_Record_WorksCorrectly() {
        // given
        RealTimeTradeService.ChangeRateResult result = new RealTimeTradeService.ChangeRateResult(5.5, true);

        // then
        assertThat(result.changeRate()).isEqualTo(5.5);
        assertThat(result.shouldUpdate()).isTrue();
    }

    @Test
    @DisplayName("ChangeRateResult record - false 케이스")
    void changeRateResult_Record_FalseCase() {
        // given
        RealTimeTradeService.ChangeRateResult result = new RealTimeTradeService.ChangeRateResult(0.0, false);

        // then
        assertThat(result.changeRate()).isEqualTo(0.0);
        assertThat(result.shouldUpdate()).isFalse();
    }

    @Test
    @DisplayName("ChangeRateResult record - equals와 hashCode 동작")
    void changeRateResult_Record_EqualsAndHashCode() {
        // given
        RealTimeTradeService.ChangeRateResult result1 = new RealTimeTradeService.ChangeRateResult(5.5, true);
        RealTimeTradeService.ChangeRateResult result2 = new RealTimeTradeService.ChangeRateResult(5.5, true);

        // then
        assertThat(result1).isEqualTo(result2);
        assertThat(result1.hashCode()).isEqualTo(result2.hashCode());
    }


    // ===== updateTradeCache 메서드 테스트 =====
    @Test
    @DisplayName("updateTradeCache - shouldUpdate가 true일 때 캐시 업데이트")
    void updateTradeCache_ShouldUpdateTrue_UpdatesCache() {
        // given
        RealTimeTradeService.ChangeRateResult changeRateResult =
                new RealTimeTradeService.ChangeRateResult(5.0, true);

        // when
        service.updateTradeCache(testTradeEventDto, changeRateResult);

        // then - 내부 상태 확인을 위해 다음 호출에서 이전 데이터로 사용되는지 확인
        TradeEventDto newTrade = new TradeEventDto("TRUMP", 2.0, 55000.0, testTime.plusSeconds(10));
        RealTimeTradeService.TradeInfo newTradeInfo = service.extractTradeInfo(newTrade);

        RealTimeTradeService.ChangeRateResult result = service.calculateChangeRate(newTrade, newTradeInfo);
        assertThat(result.shouldUpdate()).isTrue(); // 이전 데이터가 캐시되었으므로 계산 가능
    }

    @Test
    @DisplayName("updateTradeCache - shouldUpdate가 false지만 캐시에 없을 때 업데이트")
    void updateTradeCache_ShouldUpdateFalseButNoCachedData_UpdatesCache() {
        // given
        RealTimeTradeService.ChangeRateResult changeRateResult =
                new RealTimeTradeService.ChangeRateResult(0.0, false);

        // when
        service.updateTradeCache(testTradeEventDto, changeRateResult);

        // then - 캐시되었는지 확인
        TradeEventDto newTrade = new TradeEventDto("TRUMP", 2.0, 55000.0, testTime.plusSeconds(10));
        RealTimeTradeService.TradeInfo newTradeInfo = service.extractTradeInfo(newTrade);

        RealTimeTradeService.ChangeRateResult result = service.calculateChangeRate(newTrade, newTradeInfo);
        assertThat(result.shouldUpdate()).isTrue();
    }

    @Test
    @DisplayName("updateTradeCache - shouldUpdate가 false이고 캐시에 있을 때 업데이트 안함")
    void updateTradeCache_ShouldUpdateFalseAndCachedDataExists_DoesNotUpdate() {
        // given - 먼저 캐시에 데이터 추가
        RealTimeTradeService.ChangeRateResult firstResult =
                new RealTimeTradeService.ChangeRateResult(5.0, true);
        service.updateTradeCache(testTradeEventDto, firstResult);

        // 다른 가격의 거래 데이터
        TradeEventDto differentTrade = new TradeEventDto("TRUMP", 2.0, 60000.0, testTime.plusSeconds(5));
        RealTimeTradeService.ChangeRateResult secondResult =
                new RealTimeTradeService.ChangeRateResult(0.0, false);

        // when
        service.updateTradeCache(differentTrade, secondResult);

        // then - 원래 캐시된 데이터가 유지되는지 확인
        TradeEventDto thirdTrade = new TradeEventDto("TRUMP", 1.0, 55000.0, testTime.plusSeconds(10));
        RealTimeTradeService.TradeInfo thirdTradeInfo = service.extractTradeInfo(thirdTrade);

        RealTimeTradeService.ChangeRateResult result = service.calculateChangeRate(thirdTrade, thirdTradeInfo);
        // 첫 번째 캐시된 데이터(50000.0)와 비교되어야 함
        double expectedChangeRate = ((55000.0 - 50000.0) / 50000.0) * 100;
        assertThat(result.changeRate()).isCloseTo(expectedChangeRate, org.assertj.core.data.Offset.offset(0.01));
    }

    // ===== generateRealTimeData 메서드 테스트 =====
    @Test
    @DisplayName("generateRealTimeData - 정상적인 실시간 데이터 생성")
    void generateRealTimeData_ValidInput_ReturnsCorrectData() {
        // when
        RealTimeDataDto result = service.generateRealTimeData(testTradeEventDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTicker()).isEqualTo("TRUMP");
        assertThat(result.getPrice()).isEqualTo(50000.0);
        assertThat(result.getSize()).isEqualTo(1.5);
        assertThat(result.getTimestamp()).isEqualTo(testTime);
        assertThat(result.getTransactionId()).isNotNull();
        assertThat(result.getChangeRate()).isEqualTo(0.0); // 첫 번째 거래이므로 0
    }

    @Test
    @DisplayName("generateRealTimeData - 두 번째 거래에서 변동률 계산")
    void generateRealTimeData_SecondTrade_CalculatesChangeRate() {
        // given - 첫 번째 거래
        service.generateRealTimeData(testTradeEventDto);

        // 두 번째 거래 (가격 상승)
        TradeEventDto secondTrade = new TradeEventDto("TRUMP", 2.0, 55000.0, testTime.plusSeconds(10));

        // when
        RealTimeDataDto result = service.generateRealTimeData(secondTrade);

        // then
        assertThat(result.getChangeRate()).isEqualTo(10.0); // (55000-50000)/50000 * 100
    }

    @Test
    @DisplayName("generateRealTimeData - 동일한 타임스탬프 거래 처리")
    void generateRealTimeData_SameTimestamp_ReturnsZeroChangeRate() {
        // given - 첫 번째 거래
        service.generateRealTimeData(testTradeEventDto);

        // 동일한 타임스탬프의 다른 거래
        TradeEventDto sameTrade = new TradeEventDto("TRUMP", 2.0, 55000.0, testTime);

        // when
        RealTimeDataDto result = service.generateRealTimeData(sameTrade);

        // then
        assertThat(result.getChangeRate()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("generateRealTimeData - 예외 발생 시 기본값 반환")
    void generateRealTimeData_ExceptionOccurs_ReturnsDefaultData() {
        // given - null 값으로 예외 유발 가능한 데이터
        TradeEventDto nullTrade = new TradeEventDto(null, 1.0, 50000.0, testTime);

        // when
        RealTimeDataDto result = service.generateRealTimeData(nullTrade);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getChangeRate()).isEqualTo(0.0);
    }

    // ===== calculateChangeRate 메서드 테스트 =====
    @Test
    @DisplayName("calculateChangeRate - 이전 거래가 없는 경우")
    void calculateChangeRate_NoPreviousTrade_ReturnsZeroWithFalse() {
        // given
        RealTimeTradeService.TradeInfo tradeInfo = service.extractTradeInfo(testTradeEventDto);

        // when
        RealTimeTradeService.ChangeRateResult result = service.calculateChangeRate(testTradeEventDto, tradeInfo);

        // then
        assertThat(result.changeRate()).isEqualTo(0.0);
        assertThat(result.shouldUpdate()).isFalse();
    }

    @Test
    @DisplayName("calculateChangeRate - 정상적인 변동률 계산")
    void calculateChangeRate_ValidPreviousTrade_CalculatesCorrectly() {
        // given - 이전 거래 캐시에 저장
        RealTimeTradeService.ChangeRateResult firstResult =
                new RealTimeTradeService.ChangeRateResult(0.0, true);
        service.updateTradeCache(testTradeEventDto, firstResult);

        // 새로운 거래
        TradeEventDto newTrade = new TradeEventDto("TRUMP", 2.0, 55000.0, testTime.plusSeconds(10));
        RealTimeTradeService.TradeInfo newTradeInfo = service.extractTradeInfo(newTrade);

        // when
        RealTimeTradeService.ChangeRateResult result = service.calculateChangeRate(newTrade, newTradeInfo);

        // then
        assertThat(result.changeRate()).isEqualTo(10.0); // (55000-50000)/50000 * 100
        assertThat(result.shouldUpdate()).isTrue();
    }

    @Test
    @DisplayName("calculateChangeRate - 동일한 타임스탬프 거래")
    void calculateChangeRate_SameTimestamp_ReturnsZeroWithFalse() {
        // given - 이전 거래 캐시에 저장
        RealTimeTradeService.ChangeRateResult firstResult =
                new RealTimeTradeService.ChangeRateResult(0.0, true);
        service.updateTradeCache(testTradeEventDto, firstResult);

        // 동일한 타임스탬프의 거래
        TradeEventDto sameTrade = new TradeEventDto("TRUMP", 2.0, 55000.0, testTime);
        RealTimeTradeService.TradeInfo sameTradeInfo = service.extractTradeInfo(sameTrade);

        // when
        RealTimeTradeService.ChangeRateResult result = service.calculateChangeRate(sameTrade, sameTradeInfo);

        // then
        assertThat(result.changeRate()).isEqualTo(0.0);
        assertThat(result.shouldUpdate()).isFalse();
    }

    @Test
    @DisplayName("calculateChangeRate - 이전 거래 가격이 0인 경우")
    void calculateChangeRate_PreviousPriceZero_ReturnsZeroWithFalse() {
        // given - 가격이 0인 이전 거래 캐시에 저장
        TradeEventDto zeroPriceTrade = new TradeEventDto("TRUMP", 1.0, 0.0, testTime.minusSeconds(10));
        RealTimeTradeService.ChangeRateResult firstResult =
                new RealTimeTradeService.ChangeRateResult(0.0, true);
        service.updateTradeCache(zeroPriceTrade, firstResult);

        // 새로운 거래
        TradeEventDto newTrade = new TradeEventDto("TRUMP", 2.0, 55000.0, testTime);
        RealTimeTradeService.TradeInfo newTradeInfo = service.extractTradeInfo(newTrade);

        // when
        RealTimeTradeService.ChangeRateResult result = service.calculateChangeRate(newTrade, newTradeInfo);

        // then
        assertThat(result.changeRate()).isEqualTo(0.0);
        assertThat(result.shouldUpdate()).isFalse();
    }
}