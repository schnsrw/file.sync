package in.lazygod.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.lazygod.websocket.handlers.HandlerInitializer;
import in.lazygod.websocket.handlers.HandlerRegistry;
import in.lazygod.websocket.handlers.WsMessageHandler;
import in.lazygod.websocket.manager.UserSessionManager;
import in.lazygod.websocket.manager.RosterManager;
import in.lazygod.websocket.model.Packet;
import in.lazygod.websocket.model.SessionWrapper;
import in.lazygod.websocket.service.ChatService;
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
public class MainWebSocketHandler extends TextWebSocketHandler {

    private final HandlerRegistry registry;
    private final Executor executor;
    private final ChatService chatService;
    private final RosterManager rosterManager;
    private final ObjectMapper mapper = new ObjectMapper();

    public MainWebSocketHandler(HandlerRegistry registry,
                               @Qualifier("wsExecutor") Executor executor,
                               ChatService chatService,
                               RosterManager rosterManager) {
        this.registry = registry;
        this.executor = executor;
        this.chatService = chatService;
        this.rosterManager = rosterManager;
    }

    static {
        HandlerInitializer.registerAll();
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        SessionWrapper wrapper = UserSessionManager.getInstance().register(session);
        if (wrapper != null) {
            chatService.deliverPending(wrapper.getUserWrapper().getUsername());
            rosterManager.sessionJoined(wrapper.getUserWrapper().getUsername());
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        SessionWrapper wrapper = UserSessionManager.getInstance().find(session);
        if (wrapper == null) {
            wrapper = UserSessionManager.getInstance().register(session);
            if (wrapper == null) return;
            chatService.deliverPending(wrapper.getUserWrapper().getUsername());
        }

        Packet packet = mapper.readValue(message.getPayload(), Packet.class);
        WsMessageHandler handler = registry.get(packet.getType());

        if (handler == null) {
            log.warn("No handler registered for type: {}", packet.getType());
            return;
        }

        SessionWrapper finalWrapper = wrapper;
        executor.execute(() -> {
            try {
                handler.handle(finalWrapper, packet.getPayload());
            } catch (Exception e) {
                log.error("Error in handler: {}", e.getMessage());
            }
        });
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        SessionWrapper wrapper = UserSessionManager.getInstance().find(session);
        if (wrapper != null) {
            rosterManager.sessionLeft(wrapper.getUserWrapper().getUsername());
        }
        UserSessionManager.getInstance().close(session);
    }
}
