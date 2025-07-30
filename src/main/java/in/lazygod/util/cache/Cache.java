package in.lazygod.util.cache;

import java.util.Set;

/**
 * Generic cache contract.
 */
public interface Cache<K, V> {
    V get(K key);
    void put(K key, V value);
    void remove(K key);
    boolean contains(K key);
    Set<K> keys();
}
