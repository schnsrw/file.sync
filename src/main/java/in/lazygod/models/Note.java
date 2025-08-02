package in.lazygod.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Represents a user note backed by an Etherpad pad.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Note {
    @Id
    private String noteId;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    private String title;
    private String padId;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime createdOn;
    private LocalDateTime updatedOn;
}
