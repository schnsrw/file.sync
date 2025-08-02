package in.lazygod.service;

import in.lazygod.models.Note;
import in.lazygod.models.User;
import in.lazygod.repositories.NoteRepository;
import in.lazygod.security.SecurityContextHolderUtil;
import in.lazygod.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Default implementation for {@link NoteService}.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NoteServiceImpl implements NoteService {

    private final NoteRepository noteRepository;
    private final SnowflakeIdGenerator idGenerator;

    @Value("${etherpad.api-url:http://localhost:9001/api/1}")
    private String apiUrl;

    @Value("${etherpad.api-key:apikey}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public Note createNote(String title) {
        User owner = SecurityContextHolderUtil.getCurrentUser();
        String noteId = idGenerator.nextId();
        String padId = "note-" + noteId;
        createPad(padId);
        Note note = Note.builder()
                .noteId(noteId)
                .owner(owner)
                .title(title)
                .padId(padId)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
        log.info("Created note {} for user {}", noteId, owner.getUserId());
        return noteRepository.save(note);
    }

    @Override
    public List<Note> listNotes() {
        User owner = SecurityContextHolderUtil.getCurrentUser();
        return noteRepository.findByOwner(owner);
    }

    @Override
    public Note saveNote(String noteId) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new IllegalArgumentException("Note not found"));
        String text = getText(note.getPadId());
        note.setContent(text);
        note.setUpdatedOn(LocalDateTime.now());
        return noteRepository.save(note);
    }

    private void createPad(String padId) {
        String url = String.format("%s/createPad?padID=%s&apikey=%s", apiUrl, padId, apiKey);
        restTemplate.getForObject(url, Map.class);
    }

    private String getText(String padId) {
        String url = String.format("%s/getText?padID=%s&apikey=%s", apiUrl, padId, apiKey);
        Map<?, ?> response = restTemplate.getForObject(url, Map.class);
        if (response == null) return "";
        Object data = response.get("data");
        if (data instanceof Map<?, ?> dataMap) {
            Object text = dataMap.get("text");
            return text == null ? "" : text.toString();
        }
        return "";
    }
}
