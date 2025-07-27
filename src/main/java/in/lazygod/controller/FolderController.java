package in.lazygod.controller;

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
        return ResponseEntity.ok(folderService.createFolder(parentId,folderName));
    }

    @PostMapping("/{id}/favourite")
    public ResponseEntity<Void> favourite(@PathVariable("id") String folderId,
                                          @RequestParam boolean fav) {
        folderService.markFavourite(folderId, fav);
        return ResponseEntity.ok().build();
    }
}
