package in.lazygod.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

/**
 * Contract for handling websocket messages for a specific type.
 */
public interface WsMessageHandler {

    /**
     * Unique message type this handler supports.
     */
    String type();

    /**
     * Handle an incoming message payload.
     */
    void handle(WebSocketSession session, JsonNode payload) throws IOException;
}
