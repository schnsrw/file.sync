package in.lazygod.sdk.ws;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Packet {
    @JsonProperty("type")
    private String type;
    @JsonProperty("to")
    private String to;
    @JsonProperty("from")
    private String from;
    @JsonProperty("payload")
    private JsonNode payload;

    public Packet() {}

    public Packet(String type, JsonNode payload) {
        this.type = type;
        this.payload = payload;
    }

    public String getType() { return type; }
    public String getTo() { return to; }
    public String getFrom() { return from; }
    public JsonNode getPayload() { return payload; }
}
