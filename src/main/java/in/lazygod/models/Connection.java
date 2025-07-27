package in.lazygod.models;

import in.lazygod.enums.ConnectionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Connection {
    @Id
    private String connectionId;

    private String fromUserId;
    private String toUserId;

    @Enumerated(EnumType.STRING)
    private ConnectionStatus status;

    private LocalDateTime createdOn;
    private LocalDateTime updatedOn;
}
