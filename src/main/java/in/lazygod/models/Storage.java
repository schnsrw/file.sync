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
public class Storage {
    @Id
    private String storageId;

    private String storageName;
    private String basePath;
    private String storageType;

    private boolean isActive;
    private LocalDateTime createdOn;
    private LocalDateTime updatedOn;

    private String accessKey;
    private String accessId;
}
