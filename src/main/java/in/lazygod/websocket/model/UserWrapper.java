package in.lazygod.websocket.model;

import in.lazygod.util.JsonUtil;
import lombok.Getter;
import org.springframework.web.socket.WebSocketSession;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class UserWrapper {

    private final String username;
    private final Set<SessionWrapper> sessions = ConcurrentHashMap.newKeySet();
    private static final com.fasterxml.jackson.databind.ObjectMapper mapper = JsonUtil.MAPPER;

    public UserWrapper(String username) {
        this.username = username;
    }

    public void addSession(SessionWrapper sessionWrapper) {
        sessionWrapper.setUserWrapper(this);
        sessions.add(sessionWrapper);
    }

    public void removeSession(WebSocketSession session) {
        sessions.removeIf(sw -> sw.getSession().equals(session));
    }

    public boolean isOnline() {
        return !sessions.isEmpty();
    }

    public void send(Packet packet) {
        for (SessionWrapper sw : sessions) sw.sendAsync(packet);
    }
}
