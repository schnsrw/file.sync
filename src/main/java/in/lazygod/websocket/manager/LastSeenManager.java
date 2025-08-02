package in.lazygod.websocket.manager;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class LastSeenManager {

    private final static LastSeenManager SEEN_MANAGER = new LastSeenManager();

    private final ConcurrentHashMap<String, Instant> LAST_SEEN = new ConcurrentHashMap<>();

    public static LastSeenManager getInstance(){
        return SEEN_MANAGER;
    }

    public void setOnline(String username){
        LAST_SEEN.remove(username);
    }

    public void setOffline(String username){
        LAST_SEEN.put(username,Instant.now());
    }

    public Optional<Instant> getLastSeen(String username){
        return Optional.of(LAST_SEEN.getOrDefault(username,null));
    }
}
