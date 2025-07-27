package in.lazygod.websocket.manager;

import in.lazygod.security.JwtUtil;
import in.lazygod.websocket.model.Packet;
import in.lazygod.websocket.model.SessionWrapper;
import in.lazygod.websocket.model.UserWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.WebSocketSession;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class UserSessionManager {

    private static final UserSessionManager INSTANCE = new UserSessionManager();
    private static final Map<String, UserWrapper> USERS = new ConcurrentHashMap<>();

    private UserSessionManager() {}

    public static UserSessionManager getInstance() {
        return INSTANCE;
    }

    public SessionWrapper register(WebSocketSession session) {
        String query = session.getUri() != null ? session.getUri().getQuery() : null;
        if (query == null || !query.contains("token=")) return null;

        String token = extractToken(query);
        String username = JwtUtil.extractUsername(token); // JwtUtil must be static

        UserWrapper user = USERS.computeIfAbsent(username, UserWrapper::new);
        SessionWrapper wrapper = new SessionWrapper(session);
        user.addSession(wrapper);
        return wrapper;
    }

    public void close(WebSocketSession session) {
        USERS.forEach((user, wrapper) -> {
            wrapper.removeSession(session);
            if (!wrapper.isOnline()) {
                USERS.remove(user);
                log.info("User {} went offline", user);
            }
        });
    }

    public boolean isOnline(String username) {
        return USERS.containsKey(username);
    }

    public void sendToUser(String username, Packet packet) {
        UserWrapper wrapper = USERS.get(username);
        if (wrapper != null) wrapper.send(packet);
    }

    private String extractToken(String query) {
        for (String param : query.split("&")) {
            if (param.startsWith("token=")) return param.substring(6);
        }
        return null;
    }

    public Set<String> allOnlineUsers() {
        return USERS.keySet();
    }
}
