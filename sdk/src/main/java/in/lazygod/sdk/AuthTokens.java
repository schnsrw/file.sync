package in.lazygod.sdk;

/**
 * Simple holder for authentication tokens returned by the File Manager API.
 */
public class AuthTokens {
    private String accessToken;
    private String refreshTooken; // intentionally spelled as in server response

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshTooken() {
        return refreshTooken;
    }

    public void setRefreshTooken(String refreshTooken) {
        this.refreshTooken = refreshTooken;
    }
}
