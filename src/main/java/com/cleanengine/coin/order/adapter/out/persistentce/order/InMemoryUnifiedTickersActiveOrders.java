package com.cleanengine.coin.order.adapter.out.persistentce.order;

import com.cleanengine.coin.common.adapter.out.store.InMemoryKeyValueStore;
import com.cleanengine.coin.common.domain.port.KeyValueStore;
import com.cleanengine.coin.order.domain.Order;
import com.cleanengine.coin.order.domain.spi.ActiveOrders;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class InMemoryUnifiedTickersActiveOrders implements ActiveOrders {

    private final InMemoryKeyValueStore<Long, Order> activeOrders = new InMemoryKeyValueStore<>();
    private final ConcurrentHashMap<Long, ReentrantLock> orderLockMap = new ConcurrentHashMap<>();

    @Override
    public String getTicker() {
        return "";
    }

    @Override
    public void saveOrder(Order order) {
        if(order == null) throw new IllegalArgumentException("order cannot be null.");

        orderLockMap.computeIfAbsent(order.getId(), k ->{
            ReentrantLock lock = new ReentrantLock();
            activeOrders.put(order.getId(), order);
            return lock;
        });
    }

    @Override
    public Optional<Order> getOrder(Long orderId) {
        return activeOrders.get(orderId);
    }

    @Override
    public Optional<Order> removeOrder(Long orderId) {
        if(orderId == null) throw new IllegalArgumentException("orderId cannot be null.");

        Optional<Order> order = activeOrders.get(orderId);

        orderLockMap.compute(orderId, (k, v) -> {
            activeOrders.remove(orderId);
            if(v != null) v.unlock();
            return null;
        });

        return order;
    }

    @Override
    public ReentrantLock lockOrder(Long orderId) {
        if(orderId == null) throw new IllegalArgumentException("orderId cannot be null.");

        ReentrantLock lock = orderLockMap.get(orderId);
        if(lock == null) throw new IllegalArgumentException("order is not exist.");
        lock.lock();

        return lock;
    }

    @Override
    public void unlockOrder(Long orderId) {
        if(orderId == null) throw new IllegalArgumentException("orderId cannot be null.");

        ReentrantLock lock = orderLockMap.get(orderId);
        if(lock == null) throw new IllegalArgumentException("order is not exist.");
        if(lock.isHeldByCurrentThread()) lock.unlock();
    }

    @Override
    public KeyValueStore<Long, Order> getOrderKeyValueStore() {
        return activeOrders;
    }
}
