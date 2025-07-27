package in.lazygod.websocket.handlers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.lazygod.websocket.manager.UserSessionManager;
import in.lazygod.websocket.model.Packet;
import in.lazygod.websocket.model.SessionWrapper;

public class NotificationHandler implements WsMessageHandler {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String type() {
        return "notification";
    }

    @Override
    public void handle(SessionWrapper wrapper, JsonNode payload) {
        // No-op. Notifications are pushed from server side.
    }

    public static void send(String username, String from, String type, JsonNode payload) {
        Packet packet = Packet.builder()
                .from(from)
                .to(username)
                .type(type)
                .payload(payload)
                .build();
        UserSessionManager.getInstance().sendToUser(username, packet);
    }

    public static void register() {
        HandlerRegistry.getInstance().register(new NotificationHandler());
    }
}
