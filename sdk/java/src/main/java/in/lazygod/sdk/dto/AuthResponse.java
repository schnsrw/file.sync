package in.lazygod.sdk.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthResponse {
    @JsonProperty("accessToken")
    private String accessToken;
    @JsonProperty("refreshTooken")
    private String refreshToken;

    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
}
