package in.lazygod.dto;

import lombok.Data;

/**
 * Request body for creating notes.
 */
@Data
public class NoteRequest {
    private String title;
}
