package in.lazygod.dto;

import lombok.Data;

@Data
public class RefreshTokenResponse {
    public String accessToken;
    public String refreshToken;
}
