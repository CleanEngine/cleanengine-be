package com.cleanengine.coin.order.adapter.out.persistentce.order.queue;

import com.cleanengine.coin.common.adapter.out.store.InMemoryPriorityQueueStore;
import com.cleanengine.coin.common.domain.port.PriorityQueueStore;
import com.cleanengine.coin.order.domain.BuyOrder;
import com.cleanengine.coin.order.domain.Order;
import com.cleanengine.coin.order.domain.OrderType;
import com.cleanengine.coin.order.domain.SellOrder;
import com.cleanengine.coin.order.domain.spi.WaitingOrders;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class NestedInMemoryWaitingOrders implements WaitingOrders {
    private final String ticker;
    private final PriorityQueueStore<BuyOrder> limitBuyOrderPriorityQueueStore = new NestedInMemoryBuyOrderSkipListSet();
    private final PriorityQueueStore<BuyOrder> marketBuyOrderPriorityQueueStore = new InMemoryPriorityQueueStore<>();
    private final PriorityQueueStore<SellOrder> limitSellOrderPriorityQueueStore = new NestedInMemorySellOrderSkipListSet();
    private final PriorityQueueStore<SellOrder> marketSellOrderPriorityQueueStore = new InMemoryPriorityQueueStore<>();

    @Override
    public String getTicker() {
        return ticker;
    }

    @Override
    public void addOrder(Order order) {
        if(order == null) throw new IllegalArgumentException("order cannot be null.");

        if (order instanceof BuyOrder) {
            if(order.getIsMarketOrder()) {
                marketBuyOrderPriorityQueueStore.put((BuyOrder) order);
            }
            else{
                limitBuyOrderPriorityQueueStore.put((BuyOrder) order);
            }
        } else {
            if(order.getIsMarketOrder()) {
                marketSellOrderPriorityQueueStore.put((SellOrder) order);
            }
            else{
                limitSellOrderPriorityQueueStore.put((SellOrder) order);
            }
        }
    }

    @Override
    public PriorityQueueStore<BuyOrder> getBuyOrderPriorityQueueStore(OrderType orderType) {
        return orderType == OrderType.MARKET ? marketBuyOrderPriorityQueueStore : limitBuyOrderPriorityQueueStore;
    }

    @Override
    public PriorityQueueStore<SellOrder> getSellOrderPriorityQueueStore(OrderType orderType) {
        return orderType == OrderType.MARKET ? marketSellOrderPriorityQueueStore : limitSellOrderPriorityQueueStore;
    }

    @Override
    public void removeOrder(Order order) {
        if(order == null) throw new IllegalArgumentException("order cannot be null.");

        if (order instanceof BuyOrder) {
            if(order.getIsMarketOrder()) {
                marketBuyOrderPriorityQueueStore.remove((BuyOrder) order);
            }
            else{
                limitBuyOrderPriorityQueueStore.remove((BuyOrder) order);
            }
        } else {
            if(order.getIsMarketOrder()) {
                marketSellOrderPriorityQueueStore.remove((SellOrder) order);
            }
            else{
                limitSellOrderPriorityQueueStore.remove((SellOrder) order);
            }
        }
    }

    @Override
    public void removeAllByUserId(int userId) {
        limitSellOrderPriorityQueueStore.removeAllByUserId(userId);
        limitBuyOrderPriorityQueueStore.removeAllByUserId(userId);
        marketSellOrderPriorityQueueStore.removeAllByUserId(userId);
        marketBuyOrderPriorityQueueStore.removeAllByUserId(userId);
    }

    @Override
    public void clearAllQueues() {
        limitBuyOrderPriorityQueueStore.clear();
        marketBuyOrderPriorityQueueStore.clear();
        limitSellOrderPriorityQueueStore.clear();
        marketSellOrderPriorityQueueStore.clear();
    }

    @Override
    public void close() {

    }
}
