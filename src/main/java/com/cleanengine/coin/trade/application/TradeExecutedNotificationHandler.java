package com.cleanengine.coin.trade.application;

import com.cleanengine.coin.trade.entity.Trade;
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
    public void notifyAfterTradeExecuted(TradeExecutedEvent tradeExecutedEvent) {
        Trade trade = tradeExecutedEvent.getTrade();
        if (trade == null) {
            log.error("체결 알림 실패! trade == null");
            return ;
        }

        Integer sellUserId = trade.getSellUserId();
        Integer buyUserId = trade.getBuyUserId();
        if (sellUserId == null || buyUserId == null) {
            log.error("체결 알림 실패! sellUserId: {}, buyUserId: {}", sellUserId, buyUserId);
            return ;
        }

        if (sellUserId != SELL_ORDER_BOT_ID) {
            TradeExecutedNotifyDto soldDto = TradeExecutedNotifyDto.of(trade, ASK);
            messagingTemplate.convertAndSend("/topic/tradeNotification/" + sellUserId, soldDto);
        }
        if (buyUserId != BUY_ORDER_BOT_ID) {
            TradeExecutedNotifyDto boughtDto = TradeExecutedNotifyDto.of(trade, BID);
            messagingTemplate.convertAndSend("/topic/tradeNotification/" + buyUserId, boughtDto);
        }

        log.debug("{} 체결 이벤트 구독 : {}원에 {}개, 매수인: {}, 매도인: {}", trade.getTicker(), trade.getPrice(), trade.getSize(), buyUserId, sellUserId );
    }

}
