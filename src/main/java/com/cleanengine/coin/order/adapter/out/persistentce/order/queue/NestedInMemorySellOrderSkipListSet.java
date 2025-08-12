package com.cleanengine.coin.order.adapter.out.persistentce.order.queue;

import com.cleanengine.coin.common.domain.port.PriorityQueueStore;
import com.cleanengine.coin.order.domain.SellOrder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicLong;

public class NestedInMemorySellOrderSkipListSet implements PriorityQueueStore<SellOrder> {
    private final ConcurrentSkipListMap<Double, ConcurrentSkipListSet<SellOrder>> map = new ConcurrentSkipListMap<>();
    private final AtomicLong size = new AtomicLong();

    @Override
    public void put(SellOrder item) {
        if(item == null) throw new IllegalArgumentException("item cannot be null.");

        map.compute(item.getPrice(), (key, sellOrders) -> {
            if(sellOrders == null) {
                sellOrders = new ConcurrentSkipListSet<>();
            }
            boolean added = sellOrders.add(item);
            if(added) size.incrementAndGet();
            return sellOrders;
        });
    }

    @Override
    public SellOrder poll() {
        while(true) {
            Map.Entry<Double, ConcurrentSkipListSet<SellOrder>> firstEntry = map.firstEntry();

            if (firstEntry == null) {
                return null;
            }

            ConcurrentSkipListSet<SellOrder> sellOrders = firstEntry.getValue();
            try {
                SellOrder order = sellOrders.first();
                this.remove(order);
                return order;
            } catch (NoSuchElementException e) {
                continue;
            }
        }
    }

    @Override
    public SellOrder peek() {
        while(true) {
            Map.Entry<Double, ConcurrentSkipListSet<SellOrder>> firstEntry = map.firstEntry();

            if (firstEntry == null) {
                return null;
            }

            ConcurrentSkipListSet<SellOrder> sellOrders = firstEntry.getValue();
            try {
                return sellOrders.first();
            } catch (NoSuchElementException e) {
                continue;
            }
        }
    }

    @Override
    public SellOrder remove(SellOrder item) {
        if (item == null) return null;

        map.computeIfPresent(item.getPrice(), (key, orders) -> {
            boolean removed = orders.remove(item);
            if (removed) size.decrementAndGet();
            return orders.isEmpty() ? null : orders;
        });

        return item;
    }

    @Override
    public List<SellOrder> removeAllByUserId(int userId) {
        List<SellOrder> toRemove = new ArrayList<>();

        for (ConcurrentSkipListSet<SellOrder> orders : map.values()) {
            List<SellOrder> ordersToRemove = orders.stream()
                    .filter(o -> o.getUserId() == userId)
                    .toList();

            for (SellOrder order : ordersToRemove) {
                if (orders.remove(order)) {
                    toRemove.add(order);
                }
            }
        }

        int totalRemovedCount = toRemove.size();

        if (totalRemovedCount > 0) {
            size.addAndGet(-totalRemovedCount);
        }

        map.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        return toRemove;
    }

    @Override
    public long size() {
        return size.get();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public void clear() {
        map.clear();
    }
}
