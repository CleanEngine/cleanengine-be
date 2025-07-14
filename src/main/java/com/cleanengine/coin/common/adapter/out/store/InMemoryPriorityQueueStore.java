package com.cleanengine.coin.common.adapter.out.store;

import com.cleanengine.coin.common.domain.port.PriorityQueueStore;
import com.cleanengine.coin.order.domain.Order;

import java.util.concurrent.PriorityBlockingQueue;

public class InMemoryPriorityQueueStore<T extends Order & Comparable<T>> implements PriorityQueueStore<T> {
    private final PriorityBlockingQueue<T> queue = new PriorityBlockingQueue<>();

    @Override
    public void put(T item) {
        if(item == null) throw new IllegalArgumentException("item cannot be null.");
        queue.add(item);
    }

    @Override
    public T poll() {
        return queue.poll();
    }

    @Override
    public T peek() {
        return queue.peek();
    }

    @Override
    public T remove(T item) {
        if(item == null) throw new IllegalArgumentException("item cannot be null.");
        queue.remove(item);
        return item;
    }

    @Override
    public long size() {
        return queue.size();
    }

    @Override
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    @Override
    public void clear() {
        queue.clear();
    }

    @Override
    public void removeAllByUserId(int userId) {
        queue.stream().filter(order -> order.getUserId() == userId).forEach(queue::remove);
    }

}
