package com.cleanengine.coin.order.application.queue;

import com.cleanengine.coin.order.domain.BuyOrder;
import com.cleanengine.coin.order.domain.Order;
import com.cleanengine.coin.order.domain.SellOrder;
import lombok.Getter;

import java.util.concurrent.PriorityBlockingQueue;

@Getter
public class OrderQueueManager {
    private final String ticker;
    private final PriorityBlockingQueue<SellOrder> marketSellOrderQueue;
    private final PriorityBlockingQueue<SellOrder> limitSellOrderQueue;
    private final PriorityBlockingQueue<BuyOrder> marketBuyOrderQueue;
    private final PriorityBlockingQueue<BuyOrder> limitBuyOrderQueue;

    public OrderQueueManager(String ticker) {
        this.ticker = ticker;
        this.marketSellOrderQueue = new PriorityBlockingQueue<>();
        this.limitSellOrderQueue = new PriorityBlockingQueue<>();
        this.marketBuyOrderQueue = new PriorityBlockingQueue<>();
        this.limitBuyOrderQueue = new PriorityBlockingQueue<>();
    }

    public void addOrder(Order order) {
        if(order instanceof SellOrder) {
            if(order.getIsMarketOrder()) {
                marketSellOrderQueue.add((SellOrder) order);
            } else {
                limitSellOrderQueue.add((SellOrder) order);
            }
        } else {
            if(order.getIsMarketOrder()) {
                marketBuyOrderQueue.add((BuyOrder) order);
            } else {
                limitBuyOrderQueue.add((BuyOrder) order);
            }
        }
    }
}
