package in.lazygod.sdk.ws;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.net.http.WebSocket.Listener;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletionStage;

/**
 * Lightweight WebSocket client for communicating with the File Manager server.
 */
public class WebSocketClient implements Listener {

    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<String, WebSocketMessageHandler> handlers = new ConcurrentHashMap<>();
    private WebSocket webSocket;

    /** Connect to the given websocket URI. */
    public CompletableFuture<Void> connect(String uri) {
        HttpClient client = HttpClient.newHttpClient();
        return client.newWebSocketBuilder()
                .buildAsync(URI.create(uri), this)
                .thenAccept(ws -> this.webSocket = ws);
    }

    /** Send a packet. */
    public void send(Packet packet) throws JsonProcessingException {
        if (webSocket != null) {
            String text = mapper.writeValueAsString(packet);
            webSocket.sendText(text, true);
        }
    }

    /** Register a handler for a specific packet type. */
    public void registerHandler(String type, WebSocketMessageHandler handler) {
        handlers.put(type, handler);
    }

    @Override
    public void onOpen(WebSocket webSocket) {
        Listener.super.onOpen(webSocket);
        this.webSocket = webSocket;
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        try {
            Packet packet = mapper.readValue(data.toString(), Packet.class);
            WebSocketMessageHandler handler = handlers.get(packet.type);
            if (handler != null && packet.payload != null) {
                handler.handle(packet.payload);
            }
        } catch (Exception ignore) {
        }
        return Listener.super.onText(webSocket, data, last);
    }

    @Override
    public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
        return Listener.super.onBinary(webSocket, data, last);
    }

    @Override
    public void onError(WebSocket webSocket, Throwable error) {
        Listener.super.onError(webSocket, error);
    }
}
