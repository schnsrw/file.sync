package in.lazygod.models;

import jakarta.persistence.Entity;
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
public class UserRights {
    @Id
    private String urId;

    private String userId;
    private String fileId;
    private String parentFolderId;

    private String rightsType; // READ / WRITE / ADMIN
    private String resourceType; // FILE / FOLDER
    private boolean isFavourite;
    private boolean isActive;

    private LocalDateTime createdOn;
    private LocalDateTime updatedOn;
}
