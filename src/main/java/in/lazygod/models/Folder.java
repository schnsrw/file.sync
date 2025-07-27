package in.lazygod.models;


import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class Folder {

    @Id
    private String folderId;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Folder parentFolder;

    private boolean isActive;

    private String displayName;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "storage_id", nullable = false)
    private Storage storage;

    private LocalDateTime createdOn;
    private LocalDateTime updatedOn;
}
