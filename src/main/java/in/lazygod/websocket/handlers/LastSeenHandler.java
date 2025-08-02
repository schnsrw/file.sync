package in.lazygod.websocket.handlers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import in.lazygod.websocket.manager.LastSeenManager;
import in.lazygod.websocket.manager.UserSessionManager;
import in.lazygod.websocket.model.Packet;
import in.lazygod.websocket.model.Presence;
import in.lazygod.websocket.model.SessionWrapper;

import java.time.Instant;
import java.util.Optional;


public class LastSeenHandler implements WsMessageHandler{

    private final ObjectMapper mapper = new ObjectMapper();

    public static void register() {
        HandlerRegistry.getInstance().register(new LastSeenHandler());
    }

    @Override
    public String type() {
        return "last-seen";
    }

    @Override
    public void handle(SessionWrapper wrapper, JsonNode request) throws Exception {
        String to = request.has("to") ? request.get("to").asText() : null;
        String from = wrapper.getUserWrapper().getUsername();

        if(UserSessionManager.getInstance().isOnline(to)){
            ObjectNode payload = mapper.createObjectNode();
            payload.put("last-seen",-1);
            payload.put("status", Presence.ONLINE.name());

            wrapper.sendAsync(Packet.builder()
                            .from(to)
                            .to(from)
                            .type(type())
                            .payload(payload)
                            .build());
            return;
        }

        Optional<Instant> lastSeen = LastSeenManager.getInstance().getLastSeen(to);
        if(lastSeen.isPresent()) {
            ObjectNode payload = mapper.createObjectNode();
            payload.put("last-seen", lastSeen.get().toEpochMilli());
            payload.put("status", Presence.OFFLINE.name());

            wrapper.sendAsync(Packet.builder()
                            .from(to)
                            .to(from)
                            .type(type())
                            .payload(payload)
                            .build());
            return;
        }

        ObjectNode payload = mapper.createObjectNode();
        payload.put("status", Presence.OFFLINE.name());
        wrapper.sendAsync(Packet.builder()
                        .from(to)
                        .to(from)
                        .type(type())
                        .payload(payload)
                        .build());
        return;
    }
}
