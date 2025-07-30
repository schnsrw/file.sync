package in.lazygod.controller;

import in.lazygod.dto.FolderContent;
import in.lazygod.models.Folder;
import in.lazygod.service.FolderService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/folder")
@SecurityRequirement(name = "bearer-key")
@RequiredArgsConstructor
public class FolderController {

    private final FolderService folderService;

    @PostMapping
    public ResponseEntity<Folder> create(@RequestParam(required = false) String parentId,
                                         String folderName) {
        return ResponseEntity.ok(folderService.createFolder(parentId, folderName));
    }

    @PostMapping("/{id}/favourite")
    public ResponseEntity<Void> favourite(@PathVariable("id") String folderId,
                                          @RequestParam boolean fav) {
        folderService.markFavourite(folderId, fav);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/trash")
    public ResponseEntity<Void> trash(@PathVariable("id") String folderId) {
        folderService.moveToTrash(folderId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/restore")
    public ResponseEntity<Void> restore(@PathVariable("id") String folderId) {
        folderService.restore(folderId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") String folderId) throws java.io.IOException {
        folderService.deletePermanent(folderId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/list")
    public ResponseEntity<FolderContent> list(@RequestParam(required = false) String folderId,
                                              @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(folderService.listContents(folderId, page, size));
    }
}
