package in.lazygod.websocket.handlers;

import com.fasterxml.jackson.databind.JsonNode;
import in.lazygod.websocket.model.Packet;
import in.lazygod.websocket.model.SessionWrapper;

/**
 * Simple ping/pong handler.
 */
public class PingPongHandler implements WsMessageHandler {

    @Override
    public String type() {
        return "ping";
    }

    @Override
    public void handle(SessionWrapper wrapper, JsonNode payload) throws Exception {
        wrapper.sendAsync(Packet.builder().from("system").type("pong").build());
    }

    public static void register() {
        HandlerRegistry.getInstance().register(new PingPongHandler());
    }
}
