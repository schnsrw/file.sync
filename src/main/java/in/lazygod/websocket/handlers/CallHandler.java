package in.lazygod.websocket.handlers;

import com.fasterxml.jackson.databind.JsonNode;
import in.lazygod.websocket.manager.UserSessionManager;
import in.lazygod.websocket.model.Packet;
import in.lazygod.websocket.model.SessionWrapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Handles WebRTC signalling messages between peers.
 * Simply forwards call payloads to the intended recipient.
 */
@Component
@RequiredArgsConstructor
public class CallHandler implements WsMessageHandler {

    private final HandlerRegistry registry;

    @PostConstruct
    public void init() {
        registry.register(this);
    }

    @Override
    public String type() {
        return "call";
    }

    @Override
    public void handle(SessionWrapper wrapper, JsonNode payload) {
        String to = payload.has("to") ? payload.get("to").asText() : null;
        if (to == null) return;
        Packet packet = Packet.builder()
                .from(wrapper.getUserWrapper().getUsername())
                .to(to)
                .type(type())
                .payload(payload)
                .build();
        if (UserSessionManager.getInstance().isOnline(to)) {
            UserSessionManager.getInstance().sendToUser(to, packet);
        }
    }
}
