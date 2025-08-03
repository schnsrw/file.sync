package in.lazygod.models;

import in.lazygod.enums.FileRights;
import in.lazygod.enums.ResourceType;
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
public class UserRights implements Serializable {
    @Id
    private String urId;

    private String userId;
    private String fileId;
    private String parentFolderId;

    @Enumerated(EnumType.STRING)
    private FileRights rightsType;
    @Enumerated(EnumType.STRING)
    private ResourceType resourceType;
    private boolean isFavourite;
    private boolean isActive;

    private LocalDateTime createdOn;
    private LocalDateTime updatedOn;
}
