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
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class WebSocketClient implements Listener {
    private final URI uri;
    private final HttpClient client;
    private final ObjectMapper mapper = new ObjectMapper();
    private volatile WebSocket socket;
    private final Map<String, PacketHandler> handlers = new ConcurrentHashMap<>();
    private Supplier<String> tokenSupplier;
    private volatile boolean disconnectRequested = false;
    private long reconnectDelayMs = 5000;

    public WebSocketClient(String uri) {
        this.uri = URI.create(uri);
        this.client = HttpClient.newHttpClient();
    }

    public void setTokenSupplier(Supplier<String> supplier) {
        this.tokenSupplier = supplier;
    }

    public CompletableFuture<Void> connect() {
        disconnectRequested = false;
        return connectInternal();
    }

    private CompletableFuture<Void> connectInternal() {
        String token = tokenSupplier == null ? null : tokenSupplier.get();

        HttpClient httpClient = HttpClient.newHttpClient();
        HttpClient.Builder clientBuilder = HttpClient.newBuilder();

        WebSocket.Builder webSocketBuilder = httpClient.newWebSocketBuilder();
        if (token != null) {
            webSocketBuilder.header("Authorization", "Bearer " + token);
        }

        return webSocketBuilder.buildAsync(uri, this)
                .thenApply(socket -> {
                    this.socket = socket;
                    return null;
                })
                .thenRun(() -> send(new Packet("features", null)));
    }

    public void registerHandler(String type, PacketHandler handler) {
        handlers.put(type, handler);
    }

    public void close() {
        disconnectRequested = true;
        if (socket != null) {
            socket.sendClose(WebSocket.NORMAL_CLOSURE, "bye");
        }
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

    @Override
    public void onError(WebSocket webSocket, Throwable error) {
        Listener.super.onError(webSocket, error);
        scheduleReconnect();
    }

    @Override
    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
        CompletionStage<?> stage = Listener.super.onClose(webSocket, statusCode, reason);
        socket = null;
        if (!disconnectRequested) {
            scheduleReconnect();
        }
        return stage;
    }

    private void scheduleReconnect() {
        if (disconnectRequested) return;
        CompletableFuture.delayedExecutor(reconnectDelayMs, TimeUnit.MILLISECONDS)
                .execute(this::connectInternal);
    }
}
