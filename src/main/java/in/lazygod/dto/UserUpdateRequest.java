package in.lazygod.dto;

import lombok.Data;

@Data
public class UserUpdateRequest {
    private String email;
    private String fullName;
}
