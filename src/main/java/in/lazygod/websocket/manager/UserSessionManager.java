package in.lazygod.websocket.manager;

import in.lazygod.security.JwtUtil;
import in.lazygod.websocket.model.Packet;
import in.lazygod.websocket.model.SessionWrapper;
import in.lazygod.websocket.model.UserWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
public class UserSessionManager {

    private static final UserSessionManager INSTANCE = new UserSessionManager();
    private static final Map<String, UserWrapper> USERS = new ConcurrentHashMap<>();
    private static final ConcurrentMap<WebSocketSession, SessionWrapper> SESSION_MAP = new ConcurrentHashMap<>();

    private UserSessionManager() {
    }

    public static UserSessionManager getInstance() {
        return INSTANCE;
    }

    public SessionWrapper register(WebSocketSession session) {
        String query = session.getUri() != null ? session.getUri().getQuery() : null;
        if (query == null || !query.contains("token=")) return null;

        String token = extractToken(query);
        String username = JwtUtil.extractUsername(token); // JwtUtil must be static

        UserWrapper user = USERS.computeIfAbsent(username, UserWrapper::new);
        SessionWrapper wrapper = SESSION_MAP.computeIfAbsent(session, SessionWrapper::new);
        user.addSession(wrapper);
        return wrapper;
    }

    public SessionWrapper find(WebSocketSession session) {
        return SESSION_MAP.get(session);
    }

    public void close(WebSocketSession session) {
        SESSION_MAP.remove(session);
        USERS.forEach((user, wrapper) -> {
            wrapper.removeSession(session);
            if (!wrapper.isOnline()) {
                USERS.remove(user);
                LastSeenManager.getInstance().setOffline(wrapper.getUsername());
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

    public Map<String, String> getActiveSessions() {
        Map<String, String> map = new HashMap<>();
        SESSION_MAP.forEach((session, wrapper) -> {
            if (wrapper.getUserWrapper() != null) {
                map.put(session.getId(), wrapper.getUserWrapper().getUsername());
            }
        });
        return map;
    }

    public void closeById(String sessionId) {
        SESSION_MAP.keySet().stream()
                .filter(s -> s.getId().equals(sessionId))
                .findFirst()
                .ifPresent(s -> {
                    try {
                        s.close();
                    } catch (IOException ignored) {}
                    close(s);
                });
    }

    /**
     * Remove closed WebSocket sessions and their users from memory.
     * This prevents accumulation of stale sessions when clients
     * disconnect unexpectedly.
     */
    public void cleanupInactiveSessions() {
        SESSION_MAP.keySet().stream()
                .filter(session -> !session.isOpen())
                .forEach(this::close);
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
