package in.lazygod.repositories;

import in.lazygod.models.Folder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.time.LocalDateTime;

public interface FolderRepository extends JpaRepository<Folder, String> {
    List<Folder> findByParentFolder(Folder parentFolder);

    List<Folder> findByTrashedIsTrueAndTrashedOnBefore(LocalDateTime time);
}
