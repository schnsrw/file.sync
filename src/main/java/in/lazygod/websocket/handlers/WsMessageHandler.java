package in.lazygod.websocket.handlers;

import com.fasterxml.jackson.databind.JsonNode;
import in.lazygod.websocket.model.SessionWrapper;

/**
 * Contract for handling websocket messages for a specific type.
 */
public interface WsMessageHandler {
    String type();
    void handle(SessionWrapper wrapper, JsonNode payload) throws Exception;
}
