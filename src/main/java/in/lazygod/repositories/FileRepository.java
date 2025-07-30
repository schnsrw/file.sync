package in.lazygod.repositories;

import in.lazygod.models.File;
import in.lazygod.models.Folder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.time.LocalDateTime;

public interface FileRepository extends JpaRepository<File, String> {
    List<File> findByParentFolder(Folder parentFolder);

    List<File> findByParentFolderAndIsActiveTrueAndTrashedFalse(Folder parentFolder);

    List<File> findByTrashedIsTrueAndTrashedOnBefore(LocalDateTime time);
}
