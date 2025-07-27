package in.lazygod.dto;

import in.lazygod.models.File;
import in.lazygod.models.Folder;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class FolderContent {
    private List<Folder> folders;
    private List<File> files;
}
