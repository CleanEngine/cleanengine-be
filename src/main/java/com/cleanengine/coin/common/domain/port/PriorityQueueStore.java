package com.cleanengine.coin.common.domain.port;

public interface PriorityQueueStore <T extends Comparable<T>> {
    void put(T item);

    T poll();

    T peek();

    T remove(T item);

    long size();

    boolean isEmpty();

    void clear();

    void removeAllByUserId(int userId);
}
