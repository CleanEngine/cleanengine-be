package com.cleanengine.coin.order.adapter.out.persistentce.order.queue;

import com.cleanengine.coin.common.adapter.out.store.InMemoryPriorityQueueStore;
import com.cleanengine.coin.common.domain.port.PriorityQueueStore;
import com.cleanengine.coin.order.domain.BuyOrder;
import com.cleanengine.coin.order.domain.Order;
import com.cleanengine.coin.order.domain.OrderType;
import com.cleanengine.coin.order.domain.SellOrder;
import com.cleanengine.coin.order.domain.spi.WaitingOrders;
import lombok.AllArgsConstructor;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AllArgsConstructor
public class InMemoryWaitingOrders implements WaitingOrders {
    private final String ticker;
    private final InMemoryPriorityQueueStore<BuyOrder> limitBuyOrderPriorityQueueStore =
            new InMemoryPriorityQueueStore<>();
    private final InMemoryPriorityQueueStore<BuyOrder> marketBuyOrderPriorityQueueStore =
            new InMemoryPriorityQueueStore<>();
    private final InMemoryPriorityQueueStore<SellOrder> limitSellOrderPriorityQueueStore =
            new InMemoryPriorityQueueStore<>();
    private final InMemoryPriorityQueueStore<SellOrder> marketSellOrderPriorityQueueStore =
            new InMemoryPriorityQueueStore<>();

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

    public PriorityQueueStore<BuyOrder> getBuyOrderPriorityQueueStore(OrderType orderType) {
        return orderType == OrderType.MARKET ? marketBuyOrderPriorityQueueStore : limitBuyOrderPriorityQueueStore;
    }

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
    public List<Order> removeAllByUserId(int userId) {
        return Stream.of(
                        limitSellOrderPriorityQueueStore.removeAllByUserId(userId),
                        limitBuyOrderPriorityQueueStore.removeAllByUserId(userId),
                        marketSellOrderPriorityQueueStore.removeAllByUserId(userId),
                        marketBuyOrderPriorityQueueStore.removeAllByUserId(userId)
                )
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
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
