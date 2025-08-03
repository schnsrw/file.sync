package in.lazygod.repositories;

import in.lazygod.models.Note;
import in.lazygod.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * JPA repository for {@link Note} entities.
 */
public interface NoteRepository extends JpaRepository<Note, String> {
    List<Note> findByOwner(User owner);
}
