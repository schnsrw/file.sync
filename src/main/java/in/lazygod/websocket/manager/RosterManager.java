package in.lazygod.websocket.manager;

import in.lazygod.util.cache.Cache;
import in.lazygod.util.cache.LruCache;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Keeps a cached map of user rosters.
 */
public class RosterManager {
    private static final RosterManager INSTANCE = new RosterManager();
    private final Cache<String, Set<String>> rosterCache = new LruCache<>(1000, 60_000);

    private RosterManager() {}

    public static RosterManager getInstance() {
        return INSTANCE;
    }

    public synchronized void sessionJoined(String username) {
        Set<String> roster = rosterCache.get(username);
        if (roster == null) {
            roster = new HashSet<>();
            rosterCache.put(username, roster);
        }
        // add this user to others' rosters
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
