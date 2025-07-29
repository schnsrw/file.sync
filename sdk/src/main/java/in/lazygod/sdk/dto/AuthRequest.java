package in.lazygod.sdk.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthRequest {
    @JsonProperty("username")
    private String username;
    @JsonProperty("password")
    private String password;

    public AuthRequest() {}
    public AuthRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
}
