package in.lazygod.sdk;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.lazygod.sdk.handlers.PacketHandler;
import in.lazygod.sdk.ws.Packet;
import in.lazygod.sdk.ws.WebSocketClient;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class FileSyncClient {
    private final String baseUrl;
    private final HttpClient client;
    private final TokenManager tokenManager;
    private WebSocketClient wsClient;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Set<String> features = new HashSet<>();

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

    public CompletableFuture<Void> connectWebSocket() throws IOException, InterruptedException {
        wsClient = new WebSocketClient(baseUrl.replaceFirst("http", "ws") + "/ws");
        wsClient.registerHandler("features", (packet, payload) -> {
            if (payload.isArray()) {
                payload.forEach(n -> features.add(n.asText()));
            }
        });
        return wsClient.connect(tokenManager.getAccessToken());
    }

    public void registerHandler(String type, PacketHandler handler) {
        if (wsClient != null && features.contains(type)) {
            wsClient.registerHandler(type, handler);
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
