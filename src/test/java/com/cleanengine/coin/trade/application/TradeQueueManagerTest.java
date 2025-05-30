package com.cleanengine.coin.trade.application;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.cleanengine.coin.order.domain.spi.WaitingOrders;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.Level;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class TradeQueueManagerTest {

    private ListAppender<ILoggingEvent> listAppender;
    private Logger tradeQueueManagerLogger;

    @BeforeEach
    void setUp() {
        // TradeQueueManager 클래스의 로거를 가져옵니다.
        tradeQueueManagerLogger = (Logger) LoggerFactory.getLogger(TradeQueueManager.class);

        // 로그 이벤트를 캡처하기 위한 ListAppender를 설정합니다.
        listAppender = new ListAppender<>();
        // ListAppender가 올바르게 동작하기 위해 LoggerContext를 설정하는 것이 중요합니다.
        listAppender.setContext((ch.qos.logback.classic.LoggerContext) LoggerFactory.getILoggerFactory());
        listAppender.start();

        // 설정한 Appender를 로거에 추가합니다.
        tradeQueueManagerLogger.addAppender(listAppender);
        // ERROR 레벨의 로그만 캡처하도록 설정합니다 (테스트 대상이 ERROR 로그이므로).
        tradeQueueManagerLogger.setLevel(Level.ERROR);
    }

    @AfterEach
    void tearDown() {
        // 테스트 후 Appender를 정리하여 다른 테스트에 영향을 주지 않도록 합니다.
        if (tradeQueueManagerLogger != null && listAppender != null) {
            tradeQueueManagerLogger.detachAppender(listAppender);
            listAppender.stop();
        }
    }

    @DisplayName("체결 엔진 동작 중 예외 발생 시 catch 후 로깅되어야 한다.")
    @Test
    void catchExceptionWhenExecMatchAndTrade() {
        // given
        String ticker = "BTC";
        String errorMessage = "예외 발생";
        TradeFlowService mockTradeFlowService = mock(TradeFlowService.class);
        WaitingOrders mockWaitingOrders = mock(WaitingOrders.class);

        when(mockWaitingOrders.getTicker()).thenReturn(ticker);

        TradeQueueManager tradeQueueManager = new TradeQueueManager(mockWaitingOrders, mockTradeFlowService);

        doAnswer(invocation -> {
            tradeQueueManager.stop();
            throw new RuntimeException(errorMessage);
        }).when(mockTradeFlowService).execMatchAndTrade(ticker);

        // when, then
        tradeQueueManager.run();

        // then
        verify(mockTradeFlowService, times(1)).execMatchAndTrade(ticker);

        assertThat(listAppender.list).hasSize(1);
        ILoggingEvent loggingEvent = listAppender.list.get(0);

        assertThat(loggingEvent.getLevel()).isEqualTo(Level.ERROR);
        assertThat(loggingEvent.getFormattedMessage())
                .isEqualTo("Error processing trades for " + ticker + ": " + errorMessage);

    }

}