package in.lazygod.repositories;

import in.lazygod.models.Folder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FolderRepository extends JpaRepository<Folder, String> {
    List<Folder> findByParentFolder(Folder parentFolder);
}
