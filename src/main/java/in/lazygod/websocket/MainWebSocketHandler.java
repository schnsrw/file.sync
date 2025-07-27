package in.lazygod.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.lazygod.websocket.handlers.HandlerInitializer;
import in.lazygod.websocket.handlers.HandlerRegistry;
import in.lazygod.websocket.handlers.WsMessageHandler;
import in.lazygod.websocket.manager.UserSessionManager;
import in.lazygod.websocket.model.Packet;
import in.lazygod.websocket.model.SessionWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.Executor;

@Slf4j
@Component
@RequiredArgsConstructor
public class MainWebSocketHandler extends TextWebSocketHandler {

    private final HandlerRegistry registry;
    private final @Qualifier("wsExecutor") Executor executor;
    private final ObjectMapper mapper = new ObjectMapper();

    static {
        HandlerInitializer.registerAll();
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        UserSessionManager.getInstance().register(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        SessionWrapper wrapper = UserSessionManager.getInstance().find(session);
        if (wrapper == null) {
            wrapper = UserSessionManager.getInstance().register(session);
            if (wrapper == null) return;
        }

        Packet packet = mapper.readValue(message.getPayload(), Packet.class);
        WsMessageHandler handler = registry.get(packet.getType());

        if (handler == null) {
            log.warn("No handler registered for type: {}", packet.getType());
            return;
        }

        executor.execute(() -> {
            try {
                handler.handle(wrapper, packet.getPayload());
            } catch (Exception e) {
                log.error("Error in handler: {}", e.getMessage());
            }
        });
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        UserSessionManager.getInstance().close(session);
    }
}
