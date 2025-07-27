package in.lazygod.websocket.handlers;


import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry maintaining mapping between message types and their handlers.
 */
@Component
public class HandlerRegistry {

    private static HandlerRegistry INSTANCE;
    private final Map<String, WsMessageHandler> handlers = new ConcurrentHashMap<>();

    public HandlerRegistry() {
        INSTANCE = this;
    }

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
