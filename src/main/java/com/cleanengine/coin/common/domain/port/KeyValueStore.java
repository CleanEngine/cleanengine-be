package com.cleanengine.coin.common.domain.port;

import java.util.Optional;

public interface KeyValueStore<K, V> {
    void put(K key, V value);

    Optional<V> get(K key);

    Optional<V> remove(K key);

    boolean isExist(K key);

    void clear();

    boolean isEmpty();

    long size();
}
