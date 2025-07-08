package com.cleanengine.coin.order.adapter.in.event;

import com.cleanengine.coin.order.domain.Order;
import com.cleanengine.coin.order.domain.OrderStatus;
import com.cleanengine.coin.order.domain.spi.ActiveOrders;
import com.cleanengine.coin.order.domain.spi.ActiveOrdersManager;
import com.cleanengine.coin.trade.application.TradeExecutedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class SpringTradeExecutedRemoveActiveOrderHandler {
    private final ActiveOrdersManager activeOrdersManager;

    @TransactionalEventListener
    public void handleTradeExecuted(TradeExecutedEvent event) {
        String ticker = event.getTrade().getTicker();
        ActiveOrders activeOrders = activeOrdersManager.getActiveOrders(ticker);

        Order buyOrder = activeOrders.getOrder(event.getBuyOrderId()).get();
        Order sellOrder = activeOrders.getOrder(event.getSellOrderId()).get();

        if(needToBeRemovedFromActiveOrders(buyOrder)){
            activeOrders.removeOrder(buyOrder.getId());
        }

        if(needToBeRemovedFromActiveOrders(sellOrder)){
            activeOrders.removeOrder(sellOrder.getId());
        }
    }

    private boolean needToBeRemovedFromActiveOrders(Order order){
        return order.getState() == OrderStatus.DONE || order.getState() == OrderStatus.CANCELED;
    }
}
