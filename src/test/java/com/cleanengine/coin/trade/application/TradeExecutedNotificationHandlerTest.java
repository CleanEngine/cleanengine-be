package com.cleanengine.coin.trade.application;

import static com.cleanengine.coin.common.CommonValues.BUY_ORDER_BOT_ID;
import static com.cleanengine.coin.common.CommonValues.SELL_ORDER_BOT_ID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.cleanengine.coin.trade.entity.Trade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;

@DisplayName("체결 알림 단위테스트")
@ExtendWith(MockitoExtension.class)
class TradeExecutedNotificationHandlerTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    private TradeExecutedNotificationHandler handler;

    @BeforeEach
    void setUp() {
        handler = new TradeExecutedNotificationHandler(messagingTemplate);
    }

    @DisplayName("정상 체결내역을 리스닝하면 웹소켓으로 전송한다.")
    @Test
    void shouldSendNotificationsForValidTrade() {
        // given
        Trade trade = Trade.of("BTC", LocalDateTime.now(), 3, SELL_ORDER_BOT_ID, 50000.0, 1.0);
        TradeExecutedEvent event = TradeExecutedEvent.of(trade, null, null);

        // when
        handler.notifyAfterTradeExecuted(event);

        // then
        verify(messagingTemplate).convertAndSend(eq("/topic/tradeNotification/1"), any(TradeExecutedNotifyDto.class));
        verify(messagingTemplate).convertAndSend(eq("/topic/tradeNotification/3"), any(TradeExecutedNotifyDto.class));
    }

    @DisplayName("정상 체결내역을 리스닝하면 웹소켓으로 전송한다.")
    @Test
    void shouldSendNotificationsForValidTrade2() {
        // given
        Trade trade = Trade.of("BTC", LocalDateTime.now(), BUY_ORDER_BOT_ID, 3, 50000.0, 1.0);
        TradeExecutedEvent event = TradeExecutedEvent.of(trade, null, null);

        // when
        handler.notifyAfterTradeExecuted(event);

        // then
        verify(messagingTemplate).convertAndSend(eq("/topic/tradeNotification/2"), any(TradeExecutedNotifyDto.class));
        verify(messagingTemplate).convertAndSend(eq("/topic/tradeNotification/3"), any(TradeExecutedNotifyDto.class));
    }

    @DisplayName("매수인과 매도인의 userId가 null이면 메시지를 전송하지 않는다.")
    @Test
    void shouldNotSendNotificationForNullUserIds() {
        // given
        Trade trade = Trade.of("BTC", LocalDateTime.now(), null, null, 50000.0, 1.0);
        TradeExecutedEvent event = TradeExecutedEvent.of(trade, null, null);

        // when
        handler.notifyAfterTradeExecuted(event);

        // then
        verifyNoInteractions(messagingTemplate);
    }

    @DisplayName("매수인의 userId가 null이면 메시지를 전송하지 않는다.")
    @Test
    void shouldNotSendNotificationForNullBuyUserId() {
        // given
        Trade trade = Trade.of("BTC", LocalDateTime.now(), null, SELL_ORDER_BOT_ID, 50000.0, 1.0);
        TradeExecutedEvent event = TradeExecutedEvent.of(trade, null, null);

        // when
        handler.notifyAfterTradeExecuted(event);

        // then
        verifyNoInteractions(messagingTemplate);
    }

    @DisplayName("매도인의 userId가 null이면 메시지를 전송하지 않는다.")
    @Test
    void shouldNotSendNotificationForNullSellUserId() {
        // given
        Trade trade = Trade.of("BTC", LocalDateTime.now(), BUY_ORDER_BOT_ID, null, 50000.0, 1.0);
        TradeExecutedEvent event = TradeExecutedEvent.of(trade, null, null);

        // when
        handler.notifyAfterTradeExecuted(event);

        // then
        verifyNoInteractions(messagingTemplate);
    }

    @DisplayName("봇끼리의 체결은 메시지를 전송하지 않는다.")
    @Test
    void shouldNotSendNotificationForBotTrade() {
        // given
        Trade trade = Trade.of("BTC", LocalDateTime.now(), BUY_ORDER_BOT_ID, SELL_ORDER_BOT_ID, 50000.0, 1.0);
        TradeExecutedEvent event = TradeExecutedEvent.of(trade, null, null);

        // when
        handler.notifyAfterTradeExecuted(event);

        // then
        verifyNoInteractions(messagingTemplate);
    }

    @DisplayName("체결이 null이면 메시지를 전송하지 않는다.")
    @Test
    void shouldNotSendNotificationForNullTrade() {
        // given
        TradeExecutedEvent event = TradeExecutedEvent.of(null, null, null);

        // when
        handler.notifyAfterTradeExecuted(event);

        // then
        verifyNoInteractions(messagingTemplate);
    }

}