package in.lazygod.models;

import in.lazygod.enums.ACTIONS;
import in.lazygod.enums.ResourceType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
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
public class ActivityLog {
    @Id
    private String activityId;
    private String userId;
    @Enumerated(EnumType.STRING)
    private ACTIONS action;
    private String targetId;
    @Enumerated(EnumType.STRING)
    private ResourceType resourceType;
    private LocalDateTime timestamp;
}
