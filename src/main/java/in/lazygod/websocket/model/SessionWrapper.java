package in.lazygod.websocket.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Getter
@Setter
public class SessionWrapper {
    private static Executor executor;
    private final WebSocketSession session;
    private UserWrapper userWrapper;
    private final ObjectMapper mapper = new ObjectMapper();

    public static void setExecutor(Executor executor) {
        SessionWrapper.executor = executor;
    }

    public SessionWrapper(WebSocketSession session) {
        this.session = session;
    }

    public void sendAsync(Packet packet) {
        if (session.isOpen() && executor != null) {
            CompletableFuture.runAsync(() -> {
                try {
                    String text;
                    try {
                        text = mapper.writeValueAsString(packet);
                    } catch (Exception e) {
                        return;
                    }
                    session.sendMessage(new TextMessage(text));
                } catch (IOException e) {
                    // ignore
                }
            }, executor);
        }
    }
}
