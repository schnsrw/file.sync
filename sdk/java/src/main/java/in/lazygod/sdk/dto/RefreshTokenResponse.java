package in.lazygod.sdk.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RefreshTokenResponse {
    @JsonProperty("accessToken")
    private String accessToken;
    @JsonProperty("refreshToken")
    private String refreshToken;

    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
}
