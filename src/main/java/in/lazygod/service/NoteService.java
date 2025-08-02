package in.lazygod.service;

import in.lazygod.models.Note;

import java.util.List;

/**
 * Service for managing {@link Note} instances and syncing them with Etherpad.
 */
public interface NoteService {

    /**
     * Creates a new note and underlying Etherpad pad.
     *
     * @param title desired title
     * @return persisted note
     */
    Note createNote(String title);

    /**
     * Lists notes for the current user.
     */
    List<Note> listNotes();

    /**
     * Fetches content from Etherpad and stores it.
     *
     * @param noteId note identifier
     * @return updated note
     */
    Note saveNote(String noteId);
}
