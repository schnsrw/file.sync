package in.lazygod.websocket.handlers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import in.lazygod.websocket.manager.UserSessionManager;
import in.lazygod.websocket.model.Packet;
import in.lazygod.websocket.model.SessionWrapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Simple forwarding handler for WebRTC signalling messages.
 * <p>
 * The client sends signalling payloads over the regular websocket channel
 * using the "call" type. This handler forwards that payload to the
 * destination user without persisting anything.
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
        if (to == null) {
            return;
        }

        ObjectNode data = payload.deepCopy();
        data.remove("to");

        String from = wrapper.getUserWrapper().getUsername();

        if (UserSessionManager.getInstance().isOnline(to)) {
            UserSessionManager.getInstance().sendToUser(to, Packet.builder()
                    .from(from)
                    .to(to)
                    .type("call")
                    .payload(data)
                    .build());
        }
    }
}

