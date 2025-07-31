package in.lazygod.sdk.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class VerificationRequest {
    @JsonProperty("verificationCode")
    private String verificationCode;

    public VerificationRequest() {}

    public VerificationRequest(String verificationCode) {
        this.verificationCode = verificationCode;
    }

    public String getVerificationCode() { return verificationCode; }
}
