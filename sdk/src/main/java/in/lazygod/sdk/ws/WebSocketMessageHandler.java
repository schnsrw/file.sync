package in.lazygod.sdk.ws;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Functional interface for handling websocket messages for a specific type.
 */
@FunctionalInterface
public interface WebSocketMessageHandler {
    void handle(JsonNode payload);
}
