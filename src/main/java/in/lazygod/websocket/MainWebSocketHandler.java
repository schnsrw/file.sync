package in.lazygod.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Slf4j
@Component
@RequiredArgsConstructor
public class MainWebSocketHandler extends TextWebSocketHandler {

    private final HandlerRegistry registry;
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        JsonNode node = mapper.readTree(message.getPayload());
        String type = node.has("type") ? node.get("type").asText() : null;
        JsonNode payload = node.get("payload");

        if (type == null) {
            log.warn("Missing message type: {}", message.getPayload());
            return;
        }
        WsMessageHandler handler = registry.get(type);
        if (handler == null) {
            log.warn("No handler registered for type {}", type);
            return;
        }
        handler.handle(session, payload);
    }
}
