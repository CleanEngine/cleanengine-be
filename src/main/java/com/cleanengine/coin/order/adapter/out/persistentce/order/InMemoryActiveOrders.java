package com.cleanengine.coin.order.adapter.out.persistentce.order;

import com.cleanengine.coin.common.adapter.out.store.InMemoryKeyValueStore;
import com.cleanengine.coin.order.domain.BuyOrder;
import com.cleanengine.coin.order.domain.Order;
import com.cleanengine.coin.order.domain.SellOrder;
import com.cleanengine.coin.order.domain.spi.ActiveOrders;
import com.cleanengine.coin.common.domain.port.KeyValueStore;
import lombok.Getter;

import java.util.Optional;

public class InMemoryActiveOrders implements ActiveOrders {
    @Getter
    private final String ticker;
    private final InMemoryKeyValueStore<Long, BuyOrder> activeBuyOrders = new InMemoryKeyValueStore<>();
    private final InMemoryKeyValueStore<Long, SellOrder> activeSellOrders = new InMemoryKeyValueStore<>();

    public InMemoryActiveOrders(String ticker) {
        this.ticker = ticker;
    }

    @Override
    public void saveOrder(Order order) {
        if(order.getIsMarketOrder()){
            return;
        }
        if(order instanceof BuyOrder){
            activeBuyOrders.put(order.getId(), (BuyOrder) order);
        } else {
            activeSellOrders.put(order.getId(), (SellOrder) order);
        }
    }

    @Override
    public Optional<Order> getOrder(Long orderId, boolean isBuyOrder) {
        if(isBuyOrder) {
            return activeBuyOrders.get(orderId).map(order-> order);
        } else {
            return activeSellOrders.get(orderId).map(order-> order);
        }
    }

    @Override
    public Optional<Order> removeOrder(Long orderId, boolean isBuyOrder) {
        if(isBuyOrder) {
            return activeBuyOrders.remove(orderId).map(order-> order);
        } else {
            return activeSellOrders.remove(orderId).map(order-> order);
        }
    }

    @Override
    public KeyValueStore<Long, BuyOrder> getBuyOrderKeyValueStore() {
        return activeBuyOrders;
    }

    @Override
    public KeyValueStore<Long, SellOrder> getSellOrderKeyValueStore() {
        return activeSellOrders;
    }
}
