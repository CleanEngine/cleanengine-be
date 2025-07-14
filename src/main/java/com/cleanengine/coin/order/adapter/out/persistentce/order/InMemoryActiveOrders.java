package com.cleanengine.coin.order.adapter.out.persistentce.order;

import com.cleanengine.coin.common.adapter.out.store.InMemoryKeyValueStore;
import com.cleanengine.coin.common.domain.port.KeyValueStore;
import com.cleanengine.coin.order.domain.Order;
import com.cleanengine.coin.order.domain.spi.ActiveOrders;
import lombok.Getter;

import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

public class InMemoryActiveOrders implements ActiveOrders {
    @Getter
    private final String ticker;
    private final InMemoryKeyValueStore<Long, Order> activeOrders = new InMemoryKeyValueStore<>();

    public InMemoryActiveOrders(String ticker) {
        this.ticker = ticker;
    }

    @Override
    public void saveOrder(Order order) {
        if(order == null) throw new IllegalArgumentException("order cannot be null.");
        activeOrders.put(order.getId(), order);
    }

    @Override
    public Optional<Order> getOrder(Long orderId) {
        return activeOrders.get(orderId);
    }

    @Override
    public Optional<Order> removeOrder(Long orderId) {
        return activeOrders.remove(orderId);
    }

    @Override
    public ReentrantLock lockOrder(Long orderId) {
        return null;
    }

    @Override
    public void unlockOrder(Long orderId) {

    }

    @Override
    public KeyValueStore<Long, Order> getOrderKeyValueStore() {
        return activeOrders;
    }

    @Override
    public void removeAllByUserId(int userId) {
        activeOrders.forEach((orderId, order) -> {
            if (order.getUserId() == userId) {
                activeOrders.remove(orderId);
            }
        });
    }

}
