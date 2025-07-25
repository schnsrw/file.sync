package in.lazygod.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRights {
    @Id
    private UUID urId;

    private UUID userId;
    private UUID fileId;
    private UUID parentFolderId;

    private String rightsType; // READ / WRITE / ADMIN
    private String resourceType; // FILE / FOLDER
    private boolean isFavourite;
    private boolean isActive;

    private LocalDateTime createdOn;
    private LocalDateTime updatedOn;
}
