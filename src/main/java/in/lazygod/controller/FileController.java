package in.lazygod.controller;

import in.lazygod.models.File;
import in.lazygod.models.UserRights;
import in.lazygod.models.ActivityLog;
import in.lazygod.repositories.FileRepository;
import in.lazygod.repositories.UserRightsRepository;
import in.lazygod.repositories.ActivityLogRepository;
import in.lazygod.service.StorageService;
import in.lazygod.util.SnowflakeIdGenerator;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/file")
@SecurityRequirement(name = "bearer-key")
@RequiredArgsConstructor
public class FileController {

    private final StorageService storageService;
    private final FileRepository fileRepository;
    private final UserRightsRepository rightsRepository;
    private final ActivityLogRepository activityRepository;
    private final SnowflakeIdGenerator idGenerator;

    @PostMapping("/upload")
    public ResponseEntity<File> upload(@RequestParam("file") MultipartFile file,
                                       @RequestParam String folderId) throws IOException {
        String fileId = idGenerator.nextId();
        String path = storageId + "/" + fileId;
        storageService.upload(file, path);

        File entity = File.builder()
                .fileId(fileId)
                .displayName(file.getOriginalFilename())
                .storageId(storageId)
                .fileSize(file.getSize())
                .version("1")
                .path(path)
                .mimeType(file.getContentType())
                .isActive(true)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
        fileRepository.save(entity);

        UserRights rights = UserRights.builder()
                .urId(idGenerator.nextId())
                .userId(userId)
                .fileId(fileId)
                .rightsType("ADMIN")
                .resourceType("FILE")
                .isFavourite(false)
                .isActive(true)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
        rightsRepository.save(rights);

        activityRepository.save(ActivityLog.builder()
                .activityId(idGenerator.nextId())
                .userId(userId)
                .action("UPLOAD_FILE")
                .targetId(fileId)
                .timestamp(LocalDateTime.now())
                .build());

        return ResponseEntity.ok(entity);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable("id") String fileId) throws IOException {
        File file = fileRepository.findById(fileId).orElseThrow();
        Resource resource = storageService.download(file.getPath());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getDisplayName())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @PostMapping("/{id}/favourite")
    public ResponseEntity<Void> favourite(@PathVariable("id") String fileId,
                                          @RequestParam String userId,
                                          @RequestParam boolean fav) {
        UserRights rights = rightsRepository.findByUserIdAndFileId(userId, fileId)
                .orElseThrow();
        rights.setFavourite(fav);
        rights.setUpdatedOn(LocalDateTime.now());
        rightsRepository.save(rights);

        activityRepository.save(ActivityLog.builder()
                .activityId(idGenerator.nextId())
                .userId(userId)
                .action(fav ? "FAV_FILE" : "UNFAV_FILE")
                .targetId(fileId)
                .timestamp(LocalDateTime.now())
                .build());
        return ResponseEntity.ok().build();
    }
}
