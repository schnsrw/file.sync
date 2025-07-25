package in.lazygod.dto;

import in.lazygod.enums.Role;
import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String password;
    private String fullName;
    private String email;
    private Role role; // Add this
}
