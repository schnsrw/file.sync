package in.lazygod.models;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
public class File {
    @Id
    private UUID fileId;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    private String displayName;
    private UUID storageId;
    private long fileSize;

    private String version;
    private String path;
    private String mimeType;

    private boolean isActive;
    private LocalDateTime createdOn;
    private LocalDateTime updatedOn;
}
