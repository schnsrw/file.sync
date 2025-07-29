package in.lazygod.sdk;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.lazygod.sdk.ws.Packet;
import in.lazygod.sdk.ws.WebSocketClient;
import in.lazygod.sdk.ws.WebSocketMessageHandler;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

/**
 * Minimal SDK for interacting with the File Manager server.
 */
public class FileManagerSDK {

    private final String baseUrl;
    private final String username;
    private final String password;
    private final HttpClient httpClient;
    private final ObjectMapper mapper = new ObjectMapper();

    private AuthTokens tokens;
    private WebSocketClient wsClient;

    public FileManagerSDK(String baseUrl, String username, String password) throws IOException, InterruptedException {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.username = username;
        this.password = password;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        login();
    }

    /**
     * Authenticate using username/password and store the tokens.
     */
    public void login() throws IOException, InterruptedException {
        Map<String, String> body = Map.of(
                "username", username,
                "password", password
        );
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            tokens = mapper.readValue(response.body(), AuthTokens.class);
        } else {
            throw new IOException("Authentication failed with status " + response.statusCode());
        }
    }

    /** Refresh access token using the refresh token. */
    public void refresh() throws IOException, InterruptedException {
        if (tokens == null) return;
        Map<String, String> body = Map.of("refreshToken", tokens.getRefreshTooken());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/auth/refresh"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            tokens = mapper.readValue(response.body(), AuthTokens.class);
        } else {
            throw new IOException("Token refresh failed with status " + response.statusCode());
        }
    }

    /** Perform a GET request to the given path. */
    public String get(String path) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Authorization", "Bearer " + tokens.getAccessToken())
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 401) {
            refresh();
            return get(path);
        }
        return response.body();
    }

    /** Perform a POST request with JSON body. */
    public String post(String path, Object payload) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Authorization", "Bearer " + tokens.getAccessToken())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(payload)))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 401) {
            refresh();
            return post(path, payload);
        }
        return response.body();
    }

    /** Connect to websocket and request available features. */
    public void connectWebSocket() throws Exception {
        if (wsClient != null) return;
        wsClient = new WebSocketClient();
        String wsUri = baseUrl.replaceFirst("^http", "ws") + "/ws?token=" + tokens.getAccessToken();
        wsClient.connect(wsUri).join();
        Packet featureReq = new Packet();
        featureReq.type = "features";
        featureReq.to = "system";
        featureReq.from = username;
        wsClient.send(featureReq);
    }

    /** Register websocket handler for a specific message type. */
    public void registerWebSocketHandler(String type, WebSocketMessageHandler handler) {
        if (wsClient != null) wsClient.registerHandler(type, handler);
    }
}
