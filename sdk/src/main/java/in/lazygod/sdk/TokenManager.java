package in.lazygod.sdk;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.lazygod.sdk.dto.AuthRequest;
import in.lazygod.sdk.dto.AuthResponse;
import in.lazygod.sdk.dto.RefreshTokenRequest;
import in.lazygod.sdk.dto.RefreshTokenResponse;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;

public class TokenManager {
    private final String baseUrl;
    private final String username;
    private final String password;
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    private String accessToken;
    private String refreshToken;
    private Instant expiresAt;

    public TokenManager(String baseUrl, String username, String password) {
        this.baseUrl = baseUrl;
        this.username = username;
        this.password = password;
    }

    public synchronized String getAccessToken() throws IOException, InterruptedException {
        if (accessToken == null || Instant.now().isAfter(expiresAt)) {
            if (refreshToken != null) {
                refresh();
            } else {
                login();
            }
        }
        return accessToken;
    }

    private void login() throws IOException, InterruptedException {
        AuthRequest req = new AuthRequest(username, password);
        String json = mapper.writeValueAsString(req);
        HttpRequest http = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> resp = client.send(http, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
            AuthResponse tokens = mapper.readValue(resp.body(), AuthResponse.class);
            this.accessToken = tokens.getAccessToken();
            this.refreshToken = tokens.getRefreshToken();
            // crude expiration assumption: 14 minutes
            this.expiresAt = Instant.now().plusSeconds(14 * 60);
        } else {
            throw new RuntimeException("Auth failed: " + resp.statusCode());
        }
    }

    private void refresh() throws IOException, InterruptedException {
        RefreshTokenRequest req = new RefreshTokenRequest(refreshToken);
        String json = mapper.writeValueAsString(req);
        HttpRequest http = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/auth/refresh"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> resp = client.send(http, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
            RefreshTokenResponse tokens = mapper.readValue(resp.body(), RefreshTokenResponse.class);
            this.accessToken = tokens.getAccessToken();
            this.refreshToken = tokens.getRefreshToken();
            this.expiresAt = Instant.now().plusSeconds(14 * 60);
        } else {
            login();
        }
    }
}
