package in.lazygod.websocket;

import in.lazygod.util.JsonUtil;
import in.lazygod.websocket.handlers.HandlerInitializer;
import in.lazygod.websocket.handlers.HandlerRegistry;
import in.lazygod.websocket.handlers.WsMessageHandler;
import in.lazygod.websocket.manager.LastSeenManager;
import in.lazygod.websocket.manager.UserSessionManager;
import in.lazygod.websocket.manager.RosterManager;
import in.lazygod.websocket.model.Packet;
import in.lazygod.websocket.model.PacketType;
import in.lazygod.websocket.model.SessionWrapper;
import in.lazygod.websocket.model.Presence;
import in.lazygod.websocket.service.ChatService;
import in.lazygod.cluster.ClusterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.Executor;
import java.util.Set;

@Slf4j
@Component
public class MainWebSocketHandler extends TextWebSocketHandler {

    private final HandlerRegistry registry;
    private final Executor executor;
    private final ChatService chatService;
    private final RosterManager rosterManager;
    private final com.fasterxml.jackson.databind.ObjectMapper mapper = JsonUtil.MAPPER;
    private final ClusterService clusterService;

    public MainWebSocketHandler(HandlerRegistry registry,
                               @Qualifier("wsExecutor") Executor executor,
                               ChatService chatService,
                               RosterManager rosterManager,
                               ClusterService clusterService) {
        this.registry = registry;
        this.executor = executor;
        this.chatService = chatService;
        this.rosterManager = rosterManager;
        this.clusterService = clusterService;
    }

    static {
        HandlerInitializer.registerAll();
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        SessionWrapper wrapper = UserSessionManager.getInstance().register(session);
        if (wrapper != null) {
            String username = wrapper.getUserWrapper().getUsername();
            chatService.deliverPending(username);
            rosterManager.sessionJoined(username);
            clusterService.registerUser(username);
            LastSeenManager.getInstance().setOnline(username);
            broadcastLastSeen(username, Presence.ONLINE);
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
            String username = wrapper.getUserWrapper().getUsername();
            UserSessionManager.getInstance().close(session);
            if (!UserSessionManager.getInstance().isOnline(username)) {
                broadcastLastSeen(username, Presence.OFFLINE);
                rosterManager.sessionLeft(username);
                clusterService.removeUser(username);
            }
        } else {
            UserSessionManager.getInstance().close(session);
        }

    }

    private void broadcastLastSeen(String username, Presence presence) {
        Set<String> roster = rosterManager.getRoster(username);
        for (String user : roster) {
            ObjectNode payload = mapper.createObjectNode();
            payload.put("status", presence.name());
            if (presence == Presence.OFFLINE) {
                LastSeenManager.getInstance().getLastSeen(username)
                        .ifPresent(ls -> payload.put("last-seen", ls.toEpochMilli()));
            } else {
                payload.put("last-seen", -1);
            }

            Packet packet = Packet.builder()
                    .from(username)
                    .to(user)
                    .type(PacketType.LAST_SEEN.value())
                    .payload(payload)
                    .build();

            if (UserSessionManager.getInstance().isOnline(user)) {
                UserSessionManager.getInstance().sendToUser(user, packet);
            } else if (clusterService.isEnabled() && clusterService.userExists(user)) {
                clusterService.publish(user, packet);
            }
        }
    }
}
