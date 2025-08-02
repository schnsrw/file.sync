package in.lazygod.websocket.model;

/**
 * Enumerates websocket packet types exchanged between clients.
 */
public enum PacketType {
    LAST_SEEN("last-seen");

    private final String value;

    PacketType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}

