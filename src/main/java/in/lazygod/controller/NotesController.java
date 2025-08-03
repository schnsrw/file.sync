package in.lazygod.controller;

import in.lazygod.dto.NoteRequest;
import in.lazygod.models.Note;
import in.lazygod.service.NoteService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST endpoints for managing notes.
 */
@RestController
@RequestMapping("/note")
@SecurityRequirement(name = "bearer-key")
@RequiredArgsConstructor
@Slf4j
public class NotesController {

    private final NoteService noteService;

    @PostMapping
    public ResponseEntity<Note> create(@RequestBody NoteRequest request) {
        Note note = noteService.createNote(request.getTitle());
        return ResponseEntity.ok(note);
    }

    @GetMapping
    public ResponseEntity<List<Note>> list() {
        return ResponseEntity.ok(noteService.listNotes());
    }

    @PostMapping("/{id}/save")
    public ResponseEntity<Note> save(@PathVariable("id") String id) {
        Note note = noteService.saveNote(id);
        return ResponseEntity.ok(note);
    }
}
