package com.cleanengine.coin.order.adapter.out.persistentce.order.queue;

import com.cleanengine.coin.common.domain.port.PriorityQueueStore;
import com.cleanengine.coin.order.domain.BuyOrder;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicLong;

public class NestedInMemoryBuyOrderSkipListSet implements PriorityQueueStore<BuyOrder> {
    private final ConcurrentSkipListMap<Double, ConcurrentSkipListSet<BuyOrder>> map = new ConcurrentSkipListMap<>(Comparator.reverseOrder());
    private final AtomicLong size = new AtomicLong();

    @Override
    public void put(BuyOrder item) {
        if(item == null) throw new IllegalArgumentException("item cannot be null.");

        map.compute(item.getPrice(), (key, buyOrders) -> {
            if(buyOrders == null) {
                buyOrders = new ConcurrentSkipListSet<>();
            }
            boolean added = buyOrders.add(item);
            if(added) size.incrementAndGet();
            return buyOrders;
        });
    }

    @Override
    public BuyOrder poll() {
        while(true) {
            Map.Entry<Double, ConcurrentSkipListSet<BuyOrder>> firstEntry = map.firstEntry();

            if (firstEntry == null) {
                return null;
            }

            ConcurrentSkipListSet<BuyOrder> buyOrders = firstEntry.getValue();
            try {
                BuyOrder order = buyOrders.first();
                this.remove(order);
                return order;
            } catch (NoSuchElementException e) {
                continue;
            }
        }
    }

    @Override
    public BuyOrder peek() {
        while(true) {
            Map.Entry<Double, ConcurrentSkipListSet<BuyOrder>> firstEntry = map.firstEntry();

            if (firstEntry == null) {
                return null;
            }

            ConcurrentSkipListSet<BuyOrder> buyOrders = firstEntry.getValue();
            try {
                return buyOrders.first();
            } catch (NoSuchElementException e) {
                continue;
            }
        }
    }

    @Override
    public BuyOrder remove(BuyOrder item) {
        if (item == null) return null;

        map.computeIfPresent(item.getPrice(), (key, orders) -> {
            boolean removed = orders.remove(item);
            if (removed) size.decrementAndGet();
            return orders.isEmpty() ? null : orders;
        });

        return item;
    }

    @Override
    public void removeAllByUserId(int userId) {
        long totalRemovedCount = 0;

        for (ConcurrentSkipListSet<BuyOrder> orders : map.values()) {
            List<BuyOrder> ordersToRemove = orders.stream()
                    .filter(o -> o.getUserId() == userId)
                    .toList();

            for (BuyOrder order : ordersToRemove) {
                if (orders.remove(order)) {
                    ++totalRemovedCount;
                }
            }
        }

        if (totalRemovedCount > 0) {
            size.addAndGet(-totalRemovedCount);
        }

        map.entrySet().removeIf(entry -> entry.getValue().isEmpty());
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
