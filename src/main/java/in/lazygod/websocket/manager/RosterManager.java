package in.lazygod.websocket.manager;

import in.lazygod.util.cache.Cache;
import in.lazygod.util.cache.RedisCache;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Keeps a cached map of user rosters using Redis.
 */
@Component
public class RosterManager {

    private final Cache<String, Set<String>> rosterCache;

    public RosterManager(RedisTemplate<String, Object> template,
                         @Value("${roster.ttl.ms:60000}") long ttl) {
        this.rosterCache = new RedisCache<>(template, "roster:", ttl);
    }

    public synchronized void sessionJoined(String username) {
        Set<String> roster = rosterCache.get(username);
        if (roster == null) {
            roster = new HashSet<>();
            rosterCache.put(username, roster);
        }
        for (String key : rosterCache.keys()) {
            if (!key.equals(username)) {
                Set<String> r = rosterCache.get(key);
                if (r != null) r.add(username);
                roster.add(key);
            }
        }
    }

    public synchronized void sessionLeft(String username) {
        rosterCache.remove(username);
        for (String key : rosterCache.keys()) {
            Set<String> r = rosterCache.get(key);
            if (r != null) r.remove(username);
        }
    }

    public Set<String> getRoster(String username) {
        Set<String> set = rosterCache.get(username);
        return set != null ? Collections.unmodifiableSet(set) : Collections.emptySet();
    }
}
