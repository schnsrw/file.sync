package in.lazygod.websocket.handlers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.lazygod.websocket.model.Packet;
import in.lazygod.websocket.model.SessionWrapper;

/**
 * Returns the list of supported websocket features.
 */

public class FeatureHandler implements WsMessageHandler {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String type() {
        return "features";
    }

    @Override
    public void handle(SessionWrapper wrapper, JsonNode payload) throws Exception {

        JsonNode features = mapper.valueToTree(HandlerRegistry.getInstance().getHandlers().keySet());

        wrapper.sendAsync(Packet.builder()
                .from("system")
                .to(wrapper.getUserWrapper().getUsername())
                .type("features")
                .payload(features)
                .build());
    }

    public static void register() {
        HandlerRegistry.getInstance().register(new FeatureHandler());
    }
}
