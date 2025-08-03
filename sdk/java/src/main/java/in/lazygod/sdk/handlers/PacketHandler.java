package in.lazygod.sdk.handlers;

import com.fasterxml.jackson.databind.JsonNode;
import in.lazygod.sdk.ws.Packet;

@FunctionalInterface
public interface PacketHandler {
    void handle(Packet packet, JsonNode payload);
}
