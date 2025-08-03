package in.lazygod.websocket.manager;

import in.lazygod.repositories.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class LastSeenManager {

    private static LastSeenManager INSTANCE;

    private final UserRepository userRepository;

    private final ConcurrentHashMap<String, Instant> LAST_SEEN = new ConcurrentHashMap<>();

    @PostConstruct
    private void init() {
        INSTANCE = this;
    }

    public static LastSeenManager getInstance(){
        return INSTANCE;
    }

    public void setOnline(String username){
        LAST_SEEN.remove(username);
    }

    public void setOffline(String username){
        Instant now = Instant.now();
        LAST_SEEN.put(username, now);
        userRepository.findByUsername(username).ifPresent(u -> {
            u.setLastSeen(LocalDateTime.ofInstant(now, ZoneId.systemDefault()));
            userRepository.save(u);
        });
    }

    public Optional<Instant> getLastSeen(String username){
        return Optional.ofNullable(LAST_SEEN.get(username));
    }
}
