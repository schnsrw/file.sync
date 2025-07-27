package in.lazygod.stoageUtils;

import in.lazygod.models.Storage;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Factory responsible for providing {@link StorageImpl} instances.
 * <p>
 * Creating a new {@link StorageImpl} can be expensive depending on the
 * underlying implementation (e.g. establishing S3 clients). To avoid
 * repeatedly creating the same instance, we cache implementations for a
 * short time. Each cache entry is identified by the {@link Storage#getStorageId()}
 * of the configuration used to create it.
 */
public class StorageFactory {

    /** Entries stay in the cache for this duration. */
    private static final Duration CACHE_TTL = Duration.ofMinutes(10);

    /**
     * Wrapper for a cached instance with its creation timestamp.
     */
    private static class CachedEntry {
        private final StorageImpl impl;
        private final Instant created;

        CachedEntry(StorageImpl impl) {
            this.impl = impl;
            this.created = Instant.now();
        }

        boolean expired() {
            return Instant.now().isAfter(created.plus(CACHE_TTL));
        }
    }

    /** Cache keyed by storageId. */
    private static final Map<String, CachedEntry> CACHE = new ConcurrentHashMap<>();

    /**
     * Obtain a {@link StorageImpl} for the given storage configuration.
     *
     * @param config storage configuration
     * @return a cached or newly created implementation
     */
    public static StorageImpl getStorageImpl(Storage config){
        String key = config.getStorageId();
        CachedEntry cached = CACHE.get(key);
        if (cached != null && !cached.expired()) {
            return cached.impl;
        }

        StorageImpl impl;
        switch (config.getStorageType()){
            case LOCAL -> impl = new LocalStorage(config.getBasePath());
            case S3 -> impl = new S3Storage();
            default -> impl = new DummyStorage();
        }

        CACHE.put(key, new CachedEntry(impl));
        return impl;
    }
}
