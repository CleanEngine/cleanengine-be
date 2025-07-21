package com.cleanengine.coin.common.domain.port;

import java.util.List;

public interface PriorityQueueStore <T extends Comparable<T>> {
    void put(T item);

    T poll();

    T peek();

    T remove(T item);

    long size();

    boolean isEmpty();

    void clear();

    List<T> removeAllByUserId(int userId);
}
