package in.lazygod.controller;

import in.lazygod.models.ActivityLog;
import in.lazygod.models.Folder;
import in.lazygod.models.UserRights;
import in.lazygod.repositories.ActivityLogRepository;
import in.lazygod.repositories.FolderRepository;
import in.lazygod.repositories.UserRightsRepository;
import in.lazygod.util.SnowflakeIdGenerator;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/folder")
@SecurityRequirement(name = "bearer-key")
@RequiredArgsConstructor
public class FolderController {

    private final FolderRepository folderRepository;
    private final UserRightsRepository rightsRepository;
    private final ActivityLogRepository activityRepository;
    private final SnowflakeIdGenerator idGenerator;

    @PostMapping
    public ResponseEntity<Folder> create(@RequestParam String userId,
                                         @RequestParam(required = false) String parentId) {
        Folder folder = Folder.builder()
                .folderId(idGenerator.nextId())
                .parentFolder(parentId)
                .isActive(true)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
        folderRepository.save(folder);

        rightsRepository.save(UserRights.builder()
                .urId(idGenerator.nextId())
                .userId(userId)
                .parentFolderId(folder.getFolderId())
                .rightsType("ADMIN")
                .resourceType("FOLDER")
                .isFavourite(false)
                .isActive(true)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build());

        activityRepository.save(ActivityLog.builder()
                .activityId(idGenerator.nextId())
                .userId(userId)
                .action("CREATE_FOLDER")
                .targetId(folder.getFolderId())
                .timestamp(LocalDateTime.now())
                .build());

        return ResponseEntity.ok(folder);
    }

    @PostMapping("/{id}/favourite")
    public ResponseEntity<Void> favourite(@PathVariable("id") String folderId,
                                          @RequestParam String userId,
                                          @RequestParam boolean fav) {
        UserRights rights = rightsRepository.findByUserIdAndParentFolderId(userId, folderId)
                .orElseThrow();
        rights.setFavourite(fav);
        rights.setUpdatedOn(LocalDateTime.now());
        rightsRepository.save(rights);

        activityRepository.save(ActivityLog.builder()
                .activityId(idGenerator.nextId())
                .userId(userId)
                .action(fav ? "FAV_FOLDER" : "UNFAV_FOLDER")
                .targetId(folderId)
                .timestamp(LocalDateTime.now())
                .build());
        return ResponseEntity.ok().build();
    }
}
