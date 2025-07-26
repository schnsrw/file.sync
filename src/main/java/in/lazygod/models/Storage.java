package in.lazygod.models;


import jakarta.persistence.*;
import in.lazygod.enums.StorageType;
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
public class Storage {
    @Id
    private String storageId;

    /**
     * Human readable name for this storage.
     */
    private String storageName;

    /**
     * Base directory on the local filesystem where all files for this storage
     * will be kept.
     */
    private String basePath;

    @Enumerated(EnumType.STRING)
    private StorageType storageType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    private boolean isActive;
    private LocalDateTime createdOn;
    private LocalDateTime updatedOn;

    private String accessKey;
    private String accessId;
}
