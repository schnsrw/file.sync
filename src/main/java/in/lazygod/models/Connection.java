package in.lazygod.models;

import in.lazygod.enums.ConnectionStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Connection implements Serializable {
    @Id
    private String connectionId;

    private String fromUserId;
    private String toUserId;

    @Enumerated(EnumType.STRING)
    private ConnectionStatus status;

    private LocalDateTime createdOn;
    private LocalDateTime updatedOn;
}
