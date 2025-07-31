package in.lazygod.sdk.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RefreshTokenRequest {
    @JsonProperty("refreshToken")
    private String refreshToken;

    public RefreshTokenRequest() {}
    public RefreshTokenRequest(String refreshToken) { this.refreshToken = refreshToken; }
    public String getRefreshToken() { return refreshToken; }
}
