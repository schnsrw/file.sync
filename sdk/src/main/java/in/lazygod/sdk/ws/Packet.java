package in.lazygod.sdk.ws;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * DTO for websocket packets.
 */
public class Packet {
    public String type;
    public String to;
    public String from;
    public JsonNode payload;
}
