package in.lazygod.websocket.manager;

import in.lazygod.enums.ConnectionStatus;
import in.lazygod.models.User;
import in.lazygod.repositories.ConnectionRepository;
import in.lazygod.repositories.UserRepository;
import in.lazygod.util.cache.Cache;
import in.lazygod.util.cache.RedisCache;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Keeps a cached map of user rosters using Redis.
 */
@Component
public class RosterManager {

    private final Cache<String, Set<String>> rosterCache;
    private final ConnectionRepository connectionRepository;
    private final UserRepository userRepository;

    public RosterManager(RedisTemplate<String, Object> template,
                         @Value("${roster.ttl.ms:60000}") long ttl,
                         ConnectionRepository connectionRepository,
                         UserRepository userRepository) {
        this.rosterCache = new RedisCache<>(template, "roster:", ttl);
        this.connectionRepository = connectionRepository;
        this.userRepository = userRepository;
    }

    /**
     * Load the roster for a user when they join a session. The roster is
     * fetched from the database if not already cached and stored in Redis for
     * quick subsequent lookups.
     */
    public synchronized void sessionJoined(String username) {
        loadRoster(username);
    }

    /**
     * Remove the user's roster from cache when they leave.
     */
    public synchronized void sessionLeft(String username) {
        rosterCache.remove(username);
    }

    /**
     * Get the roster for the given user. If it is not present in the cache it
     * will be loaded from the database and cached.
     */
    public Set<String> getRoster(String username) {
        Set<String> set = loadRoster(username);
        return set != null ? Collections.unmodifiableSet(set) : Collections.emptySet();
    }

    private Set<String> loadRoster(String username) {
        Set<String> roster = rosterCache.get(username);
        if (roster == null) {
            roster = fetchRosterFromDb(username);
            rosterCache.put(username, roster);
        }
        return roster;
    }

    private Set<String> fetchRosterFromDb(String username) {
        return userRepository.findByUsername(username)
                .map(user -> {
                    String userId = user.getUserId();
                    Set<String> ids = new HashSet<>();
                    connectionRepository.findByToUserIdAndStatus(userId, ConnectionStatus.ACCEPTED)
                            .forEach(c -> ids.add(c.getFromUserId()));
                    connectionRepository.findByFromUserIdAndStatus(userId, ConnectionStatus.ACCEPTED)
                            .forEach(c -> ids.add(c.getToUserId()));

                    return userRepository.findAllById(ids).stream()
                            .map(User::getUsername)
                            .collect(Collectors.toSet());
                })
                .orElseGet(HashSet::new);
    }
}
