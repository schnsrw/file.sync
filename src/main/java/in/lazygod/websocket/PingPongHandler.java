package in.lazygod.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

/**
 * Simple ping/pong handler.
 */

@Component
public class PingPongHandler implements WsMessageHandler {

    private final HandlerRegistry registry;

    public PingPongHandler(HandlerRegistry registry) {
        this.registry = registry;
    }

    @PostConstruct
    public void init() {
        registry.register(this);
    }

    @Override
    public String type() {
        return "ping";
    }

    @Override
    public void handle(WebSocketSession session, JsonNode payload) throws IOException {
        session.sendMessage(new TextMessage("{\"type\":\"pong\"}"));
    }
}
