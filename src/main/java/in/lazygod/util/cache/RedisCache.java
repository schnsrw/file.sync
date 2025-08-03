package in.lazygod.util.cache;

import org.springframework.data.redis.core.RedisTemplate;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class RedisCache<K, V> implements Cache<K, V> {

    private final RedisTemplate<String, Object> template;
    private final String prefix;
    private final long ttlMillis;

    public RedisCache(RedisTemplate<String, Object> template, String prefix, long ttlMillis) {
        this.template = template;
        this.prefix = prefix == null ? "" : prefix;
        this.ttlMillis = ttlMillis;
    }

    private String wrap(K key) {
        return prefix + key;
    }

    @SuppressWarnings("unchecked")
    @Override
    public V get(K key) {
        return (V) template.opsForValue().get(wrap(key));
    }

    @Override
    public void put(K key, V value) {
        template.opsForValue().set(wrap(key), value, ttlMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public void remove(K key) {
        template.delete(wrap(key));
    }

    @Override
    public boolean contains(K key) {
        return Boolean.TRUE.equals(template.hasKey(wrap(key)));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<K> keys() {
        Set<String> keys = template.keys(prefix + "*");
        if (keys == null) return Set.of();
        return keys.stream()
                .map(k -> (K) k.substring(prefix.length()))
                .collect(Collectors.toSet());
    }
}
