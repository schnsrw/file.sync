package in.lazygod.websocket;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry maintaining mapping between message types and their handlers.
 */
@Component
public class HandlerRegistry {

    private final Map<String, WsMessageHandler> handlers = new ConcurrentHashMap<>();

    /**
     * Register a handler for a specific type.
     *
     * @param handler handler instance
     */
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
