package in.lazygod.repositories;

import in.lazygod.models.Folder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FolderRepository extends JpaRepository<Folder, String> {
}
