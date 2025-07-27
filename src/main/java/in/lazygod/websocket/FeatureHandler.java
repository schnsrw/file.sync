package in.lazygod.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Set;

/**
 * Returns the list of supported websocket features.
 */
@Component
@RequiredArgsConstructor
public class FeatureHandler implements WsMessageHandler {

    private final HandlerRegistry registry;
    private final ObjectMapper mapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        registry.register(this);
    }

    @Override
    public String type() {
        return "features";
    }

    @Override
    public void handle(WebSocketSession session, JsonNode payload) throws IOException {
        Set<String> types = registry.getHandlers().keySet();
        String response = mapper.writeValueAsString(types);
        session.sendMessage(new TextMessage("{\"type\":\"features\",\"payload\":" + response + "}"));
    }
}
