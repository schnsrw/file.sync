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
public class Folder {
    @Id
    private UUID folderId;

    private UUID parentFolder;
    private boolean isActive;

    private LocalDateTime createdOn;
    private LocalDateTime updatedOn;
}
