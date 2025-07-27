package in.lazygod.websocket.handlers;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry maintaining mapping between message types and their handlers.
 */
public class HandlerRegistry {

    private static final HandlerRegistry INSTANCE = new HandlerRegistry();
    private final Map<String, WsMessageHandler> handlers = new ConcurrentHashMap<>();

    public static HandlerRegistry getInstance() {
        return INSTANCE;
    }

    public void register(WsMessageHandler handler) {
        handlers.put(handler.type(), handler);
    }

    public WsMessageHandler get(String type) {
        return handlers.get(type);
    }

    public Map<String, WsMessageHandler> getHandlers() {
        return handlers;
    }
}
