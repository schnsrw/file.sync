package in.lazygod.sdk.ws;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.lazygod.sdk.handlers.PacketHandler;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.net.http.WebSocket.Listener;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletionStage;

public class WebSocketClient implements Listener {
    private final URI uri;
    private final HttpClient client;
    private final ObjectMapper mapper = new ObjectMapper();
    private WebSocket socket;
    private final Map<String, PacketHandler> handlers = new ConcurrentHashMap<>();

    public WebSocketClient(String uri) {
        this.uri = URI.create(uri);
        this.client = HttpClient.newHttpClient();
    }

    public CompletableFuture<Void> connect(String token) {
        return client.newWebSocketBuilder()
                .header("Authorization", "Bearer " + token)
                .buildAsync(uri, this)
                .thenAccept(ws -> this.socket = ws)
                .thenRun(() -> send(new Packet("features", null)));
    }

    public void registerHandler(String type, PacketHandler handler) {
        handlers.put(type, handler);
    }

    public void send(Packet packet) {
        try {
            String json = mapper.writeValueAsString(packet);
            socket.sendText(json, true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onOpen(WebSocket webSocket) {
        Listener.super.onOpen(webSocket);
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        try {
            Packet packet = mapper.readValue(data.toString(), Packet.class);
            JsonNode payload = packet.getPayload();
            PacketHandler handler = handlers.get(packet.getType());
            if (handler != null) {
                handler.handle(packet, payload);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Listener.super.onText(webSocket, data, last);
    }
}
