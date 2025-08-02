package in.lazygod.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionRequestResponse {
    private String connectionId;
    private String username;
    private String fullName;
}
