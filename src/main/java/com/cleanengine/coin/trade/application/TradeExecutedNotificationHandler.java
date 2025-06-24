package com.cleanengine.coin.trade.application;

import com.cleanengine.coin.order.domain.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import static com.cleanengine.coin.common.CommonValues.BUY_ORDER_BOT_ID;
import static com.cleanengine.coin.common.CommonValues.SELL_ORDER_BOT_ID;

@Slf4j
@Component
public class TradeExecutedNotificationHandler {

    private final SimpMessagingTemplate messagingTemplate;

    private static final String ASK = "ask"; // 매도
    private static final String BID = "bid"; // 매수

    public TradeExecutedNotificationHandler(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @TransactionalEventListener
    public void notifyAfterTradeExecuted(TradeOrderCompletedEvent tradeOrderCompletedEvent) {
        // TODO : 평균단가는 별도 계산해야 함
        Order order = tradeOrderCompletedEvent.getOrder();
        if (order == null) {
            log.error("체결 알림 실패! order == null");
            return ;
        }

        Integer userId = order.getUserId();
        if (userId == null) {
            log.error("체결 알림 실패! userId: {}", userId);
            return ;
        }

        if (userId != SELL_ORDER_BOT_ID && userId != BUY_ORDER_BOT_ID) {
            TradeOrderCompletedNotifyDto notifyDto = TradeOrderCompletedNotifyDto.of(order);
            messagingTemplate.convertAndSend("/topic/tradeNotification/" + userId, notifyDto);
        }
    }

}
