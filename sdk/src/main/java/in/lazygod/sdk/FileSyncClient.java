package in.lazygod.sdk;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.lazygod.sdk.handlers.PacketHandler;
import in.lazygod.sdk.ws.Packet;
import in.lazygod.sdk.ws.WebSocketClient;
import in.lazygod.sdk.dto.RegisterRequest;
import in.lazygod.sdk.dto.VerificationRequest;
import in.lazygod.sdk.dto.AuthResponse;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class FileSyncClient {
    private final String baseUrl;
    private final HttpClient client;
    private final TokenManager tokenManager;
    private static WebSocketClient wsClient;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Set<String> features = new HashSet<>();
    private final Map<String, PacketHandler> handlers = new HashMap<>();

    private FileSyncClient(String baseUrl, String username, String password) {
        this.baseUrl = baseUrl;
        this.client = HttpClient.newHttpClient();
        this.tokenManager = new TokenManager(baseUrl, username, password);
    }

    public static Builder builder() { return new Builder(); }

    public String get(String path) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Authorization", "Bearer " + tokenManager.getAccessToken())
                .GET()
                .build();
        HttpResponse<String> resp = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
            return resp.body();
        }
        throw new RuntimeException("Request failed: " + resp.statusCode());
    }

    private String post(String path) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Authorization", "Bearer " + tokenManager.getAccessToken())
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> resp = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
            return resp.body();
        }
        throw new RuntimeException("Request failed: " + resp.statusCode());
    }

    public void register(RegisterRequest request) throws IOException, InterruptedException {
        String json = mapper.writeValueAsString(request);
        HttpRequest http = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/auth/register"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> resp = client.send(http, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
            throw new RuntimeException("Registration failed: " + resp.statusCode());
        }
    }

    public void verifyOtp(String userId, String code) throws IOException, InterruptedException {
        VerificationRequest request = new VerificationRequest(code);
        String json = mapper.writeValueAsString(request);
        HttpRequest http = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/auth/" + userId + "/verify?userId=" + userId))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> resp = client.send(http, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
            AuthResponse tokens = mapper.readValue(resp.body(), AuthResponse.class);
            tokenManager.setTokens(tokens.getAccessToken(), tokens.getRefreshToken());
        } else {
            throw new RuntimeException("Verification failed: " + resp.statusCode());
        }
    }

    public synchronized CompletableFuture<Void> connectWebSocket() throws IOException, InterruptedException {
        if (wsClient == null) {
            wsClient = new WebSocketClient(baseUrl.replaceFirst("http", "ws") + "/ws");
            wsClient.setTokenSupplier(() -> tokenManager.getAccessToken());
            wsClient.registerHandler("features", (packet, payload) -> {
                if (payload.isArray()) {
                    payload.forEach(n -> {
                        String f = n.asText();
                        features.add(f);
                        PacketHandler h = handlers.get(f);
                        if (h != null) wsClient.registerHandler(f, h);
                    });
                }
            });
        }
        return wsClient.connect();
    }

    public void registerHandler(String type, PacketHandler handler) {
        handlers.put(type, handler);
        if (wsClient != null && features.contains(type)) {
            wsClient.registerHandler(type, handler);
        }
    }

    public void sendChatMessage(String to, String text) {
        if (wsClient == null) return;
        var payload = mapper.createObjectNode();
        payload.put("to", to);
        payload.put("text", text);
        wsClient.send(new Packet("chat", payload));
    }

    public void sendPing() {
        if (wsClient != null) {
            wsClient.send(new Packet("ping", null));
        }
    }

    public void requestRecent(String user, Instant before) {
        if (wsClient == null) return;
        var payload = mapper.createObjectNode();
        payload.put("user", user);
        if (before != null) payload.put("before", before.toEpochMilli());
        wsClient.send(new Packet("recent", payload));
    }

    public void requestFeatures() {
        if (wsClient != null) wsClient.send(new Packet("features", null));
    }

    // ---- Connection helpers ----

    public String requestConnection(String username) throws IOException, InterruptedException {
        return post("/connections/request/" + username);
    }

    public String acceptConnection(String id) throws IOException, InterruptedException {
        return post("/connections/" + id + "/accept");
    }

    public String rejectConnection(String id) throws IOException, InterruptedException {
        return post("/connections/" + id + "/reject");
    }

    public String listPendingConnections() throws IOException, InterruptedException {
        return get("/connections/pending");
    }

    public String listConnectedUsers(int page, int size) throws IOException, InterruptedException {
        return get("/users/connected?page=" + page + "&size=" + size);
    }

    public String disconnect(String username) throws IOException, InterruptedException {
        return post("/users/" + username + "/disconnect");
    }

    public synchronized void disconnectWebSocket() {
        if (wsClient != null) {
            wsClient.close();
            wsClient = null;
        }
    }

    public static class Builder {
        private String baseUrl;
        private String username;
        private String password;

        public Builder baseUrl(String url) { this.baseUrl = url; return this; }
        public Builder username(String u) { this.username = u; return this; }
        public Builder password(String p) { this.password = p; return this; }

        public FileSyncClient build() {
            return new FileSyncClient(baseUrl, username, password);
        }
    }
}
