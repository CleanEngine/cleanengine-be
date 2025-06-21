package com.cleanengine.coin.chart.handler;

import com.cleanengine.coin.chart.dto.PrevRateDto;
import com.cleanengine.coin.chart.dto.RealTimeDataDto;
import com.cleanengine.coin.chart.dto.TradeEventDto;
import com.cleanengine.coin.chart.service.ChartSubscriptionService;
import com.cleanengine.coin.chart.service.RealTimeDataPrevRateService;
import com.cleanengine.coin.chart.service.RealTimeTradeService;
import com.cleanengine.coin.chart.service.WebsocketSendService;
import com.cleanengine.coin.trade.application.TradeExecutedEvent;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TradeEventHandler 단위 테스트")
class TradeEventHandlerTest {

    @Mock
    private RealTimeTradeService realTimeTradeService;

    @Mock
    private WebsocketSendService websocketSendService;

    @Mock
    private ChartSubscriptionService chartSubscriptionService;

    @Mock
    private RealTimeDataPrevRateService realTimeDataPrevRateService;

    @InjectMocks
    private TradeEventHandler tradeEventHandler;

    private Trade validTrade;
    private TradeExecutedEvent validEvent;
    private LocalDateTime testTime;

    @BeforeEach
    void setUp() {
        testTime = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
        validTrade = createTrade("BTC", 50000.0, 1.5, testTime);    validEvent = TradeExecutedEvent.of(validTrade, 1L, 2L);
        validEvent = TradeExecutedEvent.of(validTrade, 1L, 2L);
    }

    // ===== handleTradeEvent 테스트 =====

    @Test
    @DisplayName("정상적인 거래 이벤트 처리 - 모든 구독자 있음")
    void handleTradeEvent_ValidEvent_AllSubscribersPresent() {
        // given
        when(chartSubscriptionService.isSubscribedToRealTimeTradeRate("BTC")).thenReturn(true);
        when(chartSubscriptionService.isSubscribedToPrevRate("BTC")).thenReturn(true);

        RealTimeDataDto realTimeDto = createRealTimeDataDto();
        PrevRateDto prevRateDto = createPrevRateDto();

        when(realTimeTradeService.generateRealTimeData(any(TradeEventDto.class))).thenReturn(realTimeDto);
        when(realTimeDataPrevRateService.generatePrevRateData(any(TradeEventDto.class))).thenReturn(prevRateDto);

        // when
        tradeEventHandler.handleTradeEvent(validEvent);

        // then
        verify(chartSubscriptionService).isSubscribedToRealTimeTradeRate("BTC");
        verify(chartSubscriptionService).isSubscribedToPrevRate("BTC");
        verify(realTimeTradeService).generateRealTimeData(any(TradeEventDto.class));
        verify(realTimeDataPrevRateService).generatePrevRateData(any(TradeEventDto.class));
        verify(websocketSendService).sendChangeRate(realTimeDto, "BTC");
        verify(websocketSendService).sendPrevRate(prevRateDto, "BTC");
    }

    @Test
    @DisplayName("정상적인 거래 이벤트 처리 - 구독자 없음")
    void handleTradeEvent_ValidEvent_NoSubscribers() {
        // given
        when(chartSubscriptionService.isSubscribedToRealTimeTradeRate("BTC")).thenReturn(false);
        when(chartSubscriptionService.isSubscribedToPrevRate("BTC")).thenReturn(false);

        // when
        tradeEventHandler.handleTradeEvent(validEvent);

        // then
        verify(chartSubscriptionService).isSubscribedToRealTimeTradeRate("BTC");
        verify(chartSubscriptionService).isSubscribedToPrevRate("BTC");
        verifyNoInteractions(realTimeTradeService);
        verifyNoInteractions(realTimeDataPrevRateService);
        verifyNoInteractions(websocketSendService);
    }

    @Test
    @DisplayName("null Trade 이벤트 처리")
    void handleTradeEvent_NullTrade_HandlesGracefully() {
        // given
        TradeExecutedEvent nullTradeEvent = TradeExecutedEvent.of(null, null, null);
        // when
        tradeEventHandler.handleTradeEvent(nullTradeEvent);

        // then
        verifyNoInteractions(chartSubscriptionService);
        verifyNoInteractions(realTimeTradeService);
        verifyNoInteractions(realTimeDataPrevRateService);
        verifyNoInteractions(websocketSendService);
    }

    @Test
    @DisplayName("실시간 트레이드 처리 중 예외 발생")
    void handleTradeEvent_RealTimeTradeException_ContinuesProcessing() {
        // given
        when(chartSubscriptionService.isSubscribedToRealTimeTradeRate("BTC")).thenReturn(true);
        when(chartSubscriptionService.isSubscribedToPrevRate("BTC")).thenReturn(true);
        when(realTimeTradeService.generateRealTimeData(any(TradeEventDto.class)))
                .thenThrow(new RuntimeException("실시간 데이터 생성 실패"));

        PrevRateDto prevRateDto = createPrevRateDto();
        when(realTimeDataPrevRateService.generatePrevRateData(any(TradeEventDto.class))).thenReturn(prevRateDto);

        // when
        tradeEventHandler.handleTradeEvent(validEvent);

        // then
        verify(realTimeTradeService).generateRealTimeData(any(TradeEventDto.class));
        verify(realTimeDataPrevRateService).generatePrevRateData(any(TradeEventDto.class));
        verify(websocketSendService, never()).sendChangeRate(any(), any());
        verify(websocketSendService).sendPrevRate(prevRateDto, "BTC");
    }

    @Test
    @DisplayName("전일 대비 변동률 처리 중 예외 발생")
    void handleTradeEvent_PrevRateException_ContinuesProcessing() {
        // given
        when(chartSubscriptionService.isSubscribedToRealTimeTradeRate("BTC")).thenReturn(true);
        when(chartSubscriptionService.isSubscribedToPrevRate("BTC")).thenReturn(true);

        RealTimeDataDto realTimeDto = createRealTimeDataDto();
        when(realTimeTradeService.generateRealTimeData(any(TradeEventDto.class))).thenReturn(realTimeDto);
        when(realTimeDataPrevRateService.generatePrevRateData(any(TradeEventDto.class)))
                .thenThrow(new RuntimeException("전일 대비 변동률 생성 실패"));

        // when
        tradeEventHandler.handleTradeEvent(validEvent);

        // then
        verify(realTimeTradeService).generateRealTimeData(any(TradeEventDto.class));
        verify(realTimeDataPrevRateService).generatePrevRateData(any(TradeEventDto.class));
        verify(websocketSendService).sendChangeRate(realTimeDto, "BTC");
        verify(websocketSendService, never()).sendPrevRate(any(), any());
    }

    // ===== processRealTimeTradeRate 테스트 =====

    @Test
    @DisplayName("실시간 트레이드 처리 - 구독자 있음")
    void processRealTimeTradeRate_WithSubscribers_SendsData() {
        // given
        TradeEventDto tradeEventDto = createTradeEventDto();
        RealTimeDataDto realTimeDto = createRealTimeDataDto();

        when(chartSubscriptionService.isSubscribedToRealTimeTradeRate("BTC")).thenReturn(true);
        when(realTimeTradeService.generateRealTimeData(tradeEventDto)).thenReturn(realTimeDto);

        // when
        tradeEventHandler.processRealTimeTradeRate("BTC", tradeEventDto);

        // then
        verify(chartSubscriptionService).isSubscribedToRealTimeTradeRate("BTC");
        verify(realTimeTradeService).generateRealTimeData(tradeEventDto);
        verify(websocketSendService).sendChangeRate(realTimeDto, "BTC");
    }

    @Test
    @DisplayName("실시간 트레이드 처리 - 구독자 없음")
    void processRealTimeTradeRate_NoSubscribers_SkipsProcessing() {
        // given
        TradeEventDto tradeEventDto = createTradeEventDto();
        when(chartSubscriptionService.isSubscribedToRealTimeTradeRate("BTC")).thenReturn(false);

        // when
        tradeEventHandler.processRealTimeTradeRate("BTC", tradeEventDto);

        // then
        verify(chartSubscriptionService).isSubscribedToRealTimeTradeRate("BTC");
        verifyNoInteractions(realTimeTradeService);
        verifyNoInteractions(websocketSendService);
    }

    @Test
    @DisplayName("실시간 트레이드 처리 중 예외 발생 시 로그 출력")
    void processRealTimeTradeRate_Exception_LogsError() {
        // given
        TradeEventDto tradeEventDto = createTradeEventDto();
        when(chartSubscriptionService.isSubscribedToRealTimeTradeRate("BTC")).thenReturn(true);
        when(realTimeTradeService.generateRealTimeData(tradeEventDto))
                .thenThrow(new RuntimeException("데이터 생성 실패"));

        // when
        tradeEventHandler.processRealTimeTradeRate("BTC", tradeEventDto);

        // then
        verify(realTimeTradeService).generateRealTimeData(tradeEventDto);
        verify(websocketSendService, never()).sendChangeRate(any(), any());
    }

    // ===== processPrevRateData 테스트 =====

    @Test
    @DisplayName("전일 대비 변동률 처리 - 구독자 있음")
    void processPrevRateData_WithSubscribers_SendsData() {
        // given
        TradeEventDto tradeEventDto = createTradeEventDto();
        PrevRateDto prevRateDto = createPrevRateDto();

        when(chartSubscriptionService.isSubscribedToPrevRate("BTC")).thenReturn(true);
        when(realTimeDataPrevRateService.generatePrevRateData(tradeEventDto)).thenReturn(prevRateDto);

        // when
        tradeEventHandler.processPrevRateData("BTC", tradeEventDto);

        // then
        verify(chartSubscriptionService).isSubscribedToPrevRate("BTC");
        verify(realTimeDataPrevRateService).generatePrevRateData(tradeEventDto);
        verify(websocketSendService).sendPrevRate(prevRateDto, "BTC");
    }

    @Test
    @DisplayName("전일 대비 변동률 처리 - 구독자 없음")
    void processPrevRateData_NoSubscribers_SkipsProcessing() {
        // given
        TradeEventDto tradeEventDto = createTradeEventDto();
        when(chartSubscriptionService.isSubscribedToPrevRate("BTC")).thenReturn(false);

        // when
        tradeEventHandler.processPrevRateData("BTC", tradeEventDto);

        // then
        verify(chartSubscriptionService).isSubscribedToPrevRate("BTC");
        verifyNoInteractions(realTimeDataPrevRateService);
        verifyNoInteractions(websocketSendService);
    }

    @Test
    @DisplayName("전일 대비 변동률 처리 중 예외 발생 시 로그 출력")
    void processPrevRateData_Exception_LogsError() {
        // given
        TradeEventDto tradeEventDto = createTradeEventDto();
        when(chartSubscriptionService.isSubscribedToPrevRate("BTC")).thenReturn(true);
        when(realTimeDataPrevRateService.generatePrevRateData(tradeEventDto))
                .thenThrow(new RuntimeException("변동률 계산 실패"));

        // when
        tradeEventHandler.processPrevRateData("BTC", tradeEventDto);

        // then
        verify(realTimeDataPrevRateService).generatePrevRateData(tradeEventDto);
        verify(websocketSendService, never()).sendPrevRate(any(), any());
    }

    // ===== getTradeEventDto 정적 메서드 테스트 =====

    @Test
    @DisplayName("Trade에서 TradeEventDto 생성")
    void getTradeEventDto_ValidTrade_CreatesDto() {
        // when
        TradeEventDto result = TradeEventHandler.getTradeEventDto(validTrade);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTicker()).isEqualTo("BTC");
        assertThat(result.getSize()).isEqualTo(1.5);
        assertThat(result.getPrice()).isEqualTo(50000.0);
        assertThat(result.getTimestamp()).isEqualTo(testTime);
    }
    // ===== 헬퍼 메서드 =====

    private Trade createTrade(String ticker, Double price, Double size, LocalDateTime tradeTime) {
        return new Trade(
                1,              // id
                ticker,         // ticker
                tradeTime,      // tradeTime
                1,              // buyUserId
                2,              // sellUserId
                price,          // price
                size            // size
        );
    }

    private TradeEventDto createTradeEventDto() {
        return new TradeEventDto("BTC", 1.5, 50000.0, testTime);
    }

    private RealTimeDataDto createRealTimeDataDto() {
        return new RealTimeDataDto(
                "BTC",
                1.5,
                50000.0,
                2.5,
                testTime,
                "test-transaction-id"
        );
    }

    private PrevRateDto createPrevRateDto() {
        return new PrevRateDto(
                "BTC",
                50000.0,
                48000.0,
                4.17,
                testTime
        );
    }
}