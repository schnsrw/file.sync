package in.lazygod.util.cache;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Simple LRU cache implementation with TTL support.
 */
public class LruCache<K, V> implements Cache<K, V> {

    private final long ttlMillis;
    private final Map<K, CacheEntry<V>> map;

    public LruCache(int maxSize, long ttlMillis) {
        this.ttlMillis = ttlMillis;
        this.map = new LinkedHashMap<K, CacheEntry<V>>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, CacheEntry<V>> eldest) {
                return size() > maxSize;
            }
        };
    }

    private static class CacheEntry<V> {
        V value;
        long time;
        CacheEntry(V value, long time) {
            this.value = value;
            this.time = time;
        }
    }

    @Override
    public synchronized V get(K key) {
        CacheEntry<V> entry = map.get(key);
        if (entry == null) return null;
        if (ttlMillis > 0 && System.currentTimeMillis() - entry.time > ttlMillis) {
            map.remove(key);
            return null;
        }
        return entry.value;
    }

    @Override
    public synchronized void put(K key, V value) {
        map.put(key, new CacheEntry<>(value, System.currentTimeMillis()));
    }

    @Override
    public synchronized void remove(K key) {
        map.remove(key);
    }

    @Override
    public synchronized boolean contains(K key) {
        return get(key) != null;
    }

    @Override
    public synchronized Set<K> keys() {
        return new HashSet<>(map.keySet());
    }
}
