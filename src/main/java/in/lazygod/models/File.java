package in.lazygod.models;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
public class File {
    @Id
    private String fileId;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    private String displayName;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "storage_id", nullable = false)
    private Storage storage;

    private long fileSize;

    private String version;
    private String path;
    private String mimeType;

    private boolean isActive;
    private LocalDateTime createdOn;
    private LocalDateTime updatedOn;
}
