package com.cleanengine.coin.common.adapter.out.store;

import com.cleanengine.coin.common.domain.port.KeyValueStore;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryKeyValueStore<K, V> implements KeyValueStore<K, V> {
    private final Map<K, V> map = new ConcurrentHashMap<>();

    @Override
    public void put(K key, V value) {
        map.put(key, value);
    }

    @Override
    public Optional<V> get(K key) {
        return Optional.ofNullable(map.get(key));
    }

    @Override
    public Optional<V> remove(K key) {
        return Optional.ofNullable(map.remove(key));
    }

    @Override
    public boolean isExist(K key) {
        return map.containsKey(key);
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public long size() {
        return map.size();
    }
}
